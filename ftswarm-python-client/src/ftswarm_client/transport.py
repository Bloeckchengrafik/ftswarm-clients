from __future__ import annotations

import logging
import re
import time
from collections import defaultdict, deque
from collections.abc import Callable
from concurrent.futures import Future
from threading import Event, Lock, Thread, current_thread
from typing import Protocol

from .generated.types import VENDOR_IDS
from .protocol import FtSwarmError


logger = logging.getLogger(__name__)
serial_logger = logging.getLogger(f"{__name__}.serial")
swarm_logger = logging.getLogger(f"{__name__}.swarm")
ERROR_LINE = re.compile(r"^\s*(?:\^|\[ERROR]).*")
SUBSCRIPTION_LINE = re.compile(r"^S: (\S+) (.*)$")


class SerialConnection(Protocol):
    in_waiting: int
    out_waiting: int

    def read(self, size: int = 1) -> bytes: ...
    def write(self, data: bytes) -> int: ...
    def close(self) -> None: ...


def discover_ports() -> list[str]:
    from serial.tools import list_ports

    return [
        port.device
        for port in list_ports.comports()
        if (port.vid, port.pid) in VENDOR_IDS
    ]


def get_ftswarm_port() -> str:
    try:
        return discover_ports()[0]
    except IndexError as error:
        raise RuntimeError("No ftSwarm device found") from error


class SerialTransport:
    @classmethod
    def open(cls, port: str | None = None, timeout: float = 5.0) -> SerialTransport:
        import serial

        connection = serial.Serial(port or get_ftswarm_port(), baudrate=115200, timeout=0)
        transport = cls(connection)
        try:
            transport.initialize(timeout)
        except BaseException:
            transport.close()
            raise
        return transport

    def __init__(self, serial: SerialConnection) -> None:
        self._serial = serial
        self._write_lock = Lock()
        self._pending_lock = Lock()
        self._pending: deque[Future[str]] = deque()
        self._subscribers: dict[str, list[Callable[[str], None]]] = defaultdict(list)
        self._subscribers_lock = Lock()
        self._initialized = Event()
        self._closed = Event()
        self._failure: BaseException | None = None
        self._line = bytearray()
        self._reader = Thread(target=self._reader_loop, name="ftswarm-serial-reader", daemon=True)
        self._reader.start()

    def initialize(self, timeout: float = 5.0) -> None:
        self._write(b"\r\n")
        while self._serial.out_waiting > 0:
            time.sleep(0.001)
        self._write(b"startCLI\r\n")
        if not self._initialized.wait(timeout):
            raise TimeoutError("Timed out waiting for ftSwarm CLI initialization")
        self._raise_if_failed()

    def command(self, command: str) -> Future[str]:
        self._raise_if_failed()
        result: Future[str] = Future()
        with self._write_lock:
            with self._pending_lock:
                self._pending.append(result)
            try:
                self._write_unlocked(command.rstrip("\r\n").encode() + b"\r\n")
            except BaseException as error:
                self._fail(error)
                raise
        return result

    def subscribe(self, port: str, callback: Callable[[str], None]) -> None:
        with self._subscribers_lock:
            self._subscribers[port].append(callback)

    def close(self) -> None:
        if self._closed.is_set():
            return
        self._closed.set()
        self._serial.close()
        self._fail(RuntimeError("Serial transport closed"))
        if current_thread() is not self._reader:
            self._reader.join(timeout=1.0)

    def _reader_loop(self) -> None:
        try:
            while not self._closed.is_set():
                waiting = self._serial.in_waiting
                if waiting < 0:
                    raise OSError("Serial port is unavailable")
                chunk = self._serial.read(max(1, min(1024, waiting)))
                if not chunk:
                    time.sleep(0.01)
                    continue
                serial_logger.debug("RX %d bytes: %s", len(chunk), _debug_bytes(chunk))
                for byte in chunk:
                    self._accept_byte(byte)
        except BaseException as error:
            if not self._closed.is_set():
                logger.exception("Serial transport failed")
                self._fail(error)

    def _accept_byte(self, byte: int) -> None:
        if byte == ord("\n"):
            line = self._line.decode(errors="replace").removesuffix("\r")
            self._line.clear()
            self._handle_line(line)
            return

        self._line.append(byte)
        if not self._initialized.is_set() and self._line == b"@@@":
            self._line.clear()
            self._initialized.set()

    def _handle_line(self, line: str) -> None:
        if not self._initialized.is_set():
            if line.startswith("@@@"):
                self._initialized.set()
            elif line:
                swarm_logger.info(line)
            return

        if line.startswith("R: "):
            self._complete(line.removeprefix("R: "))
        elif ERROR_LINE.fullmatch(line):
            message = line.lstrip().removeprefix("^").lstrip()
            self._complete(error=FtSwarmError(message))
        elif line.startswith("S: "):
            self._subscription(line)
        elif line:
            swarm_logger.info(line)

    def _complete(self, value: str | None = None, error: BaseException | None = None) -> None:
        with self._pending_lock:
            future = self._pending.popleft() if self._pending else None
        if future is None:
            logger.warning("Unexpected command result with no pending command: %s", value or error)
        elif error is not None:
            future.set_exception(error)
        else:
            future.set_result(value or "")

    def _subscription(self, line: str) -> None:
        match = SUBSCRIPTION_LINE.fullmatch(line)
        if match is None:
            logger.warning("Invalid subscription: %s", line)
            return
        port, entry = match.groups()
        with self._subscribers_lock:
            callbacks = tuple(self._subscribers.get(port, ()))
        for callback in callbacks:
            try:
                callback(entry)
            except Exception:
                logger.exception("Subscription callback failed for %s", port)

    def _write(self, data: bytes) -> None:
        with self._write_lock:
            self._write_unlocked(data)

    def _write_unlocked(self, data: bytes) -> None:
        serial_logger.debug("TX %d bytes: %s", len(data), _debug_bytes(data))
        offset = 0
        while offset < len(data):
            written = self._serial.write(data[offset:])
            if written <= 0:
                raise OSError(f"Serial write failed: {written}")
            offset += written

    def _fail(self, error: BaseException) -> None:
        if self._failure is None:
            self._failure = error
        self._initialized.set()
        with self._pending_lock:
            pending = tuple(self._pending)
            self._pending.clear()
        for future in pending:
            if not future.done():
                future.set_exception(error)

    def _raise_if_failed(self) -> None:
        if self._failure is not None:
            raise RuntimeError("Serial transport failed") from self._failure


def _debug_bytes(value: bytes) -> str:
    return "".join(
        {13: "\\r", 10: "\\n", 9: "\\t"}.get(byte, chr(byte) if 0x20 <= byte <= 0x7E else f"\\x{byte:02X}")
        for byte in value
    )
