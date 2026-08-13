from __future__ import annotations

import asyncio
import time
import unittest
from collections import deque
from threading import Lock

from ftswarm_client import AsyncFtSwarmClient, FtSwarmClient, MicrostepMode
from ftswarm_client.transport import SerialTransport


class FakeSerial:
    def __init__(self) -> None:
        self._received = bytearray()
        self._input = deque[int]()
        self._lock = Lock()
        self.commands: list[str] = []
        self.out_waiting = 0
        self.closed = False

    @property
    def in_waiting(self) -> int:
        with self._lock:
            return len(self._input)

    def read(self, size: int = 1) -> bytes:
        with self._lock:
            result = bytes(self._input.popleft() for _ in range(min(size, len(self._input))))
        return result

    def write(self, data: bytes) -> int:
        self._received.extend(data)
        while b"\n" in self._received:
            raw_line, _, remainder = self._received.partition(b"\n")
            self._received = bytearray(remainder)
            command = raw_line.removesuffix(b"\r").decode()
            if command:
                self._handle(command)
        return len(data)

    def close(self) -> None:
        self.closed = True

    def emit(self, line: str) -> None:
        self._enqueue(f"{line}\r\n".encode())

    def _handle(self, command: str) -> None:
        self.commands.append(command)
        if command == "startCLI":
            self._enqueue(b"@@@ ftSwarmOS CLI started\r\n")
        elif command.endswith(".getValue()"):
            self.emit("R: 0")
        elif command == "ftSwarm400.getMicrostepMode()":
            self.emit("R: 7")
        elif command == "swarm.getSwarm(0)":
            self.emit('R: "test swarm"')
        else:
            self.emit("R: 1 ok")

    def _enqueue(self, data: bytes) -> None:
        with self._lock:
            self._input.extend(data)


def open_transport() -> tuple[SerialTransport, FakeSerial]:
    serial = FakeSerial()
    transport = SerialTransport(serial)
    transport.initialize()
    return transport, serial


class SyncClientTest(unittest.TestCase):
    def test_generated_factory_commands_and_subscription_state(self) -> None:
        transport, serial = open_transport()
        try:
            client = FtSwarmClient(transport)
            button = client.button("S1")

            self.assertFalse(button.value.value)
            self.assertEqual(
                serial.commands,
                ["startCLI", "S1.getValue()", "S1.subscribe()"],
            )

            serial.emit("S: S1 1")
            deadline = time.monotonic() + 1
            while not button.value.value and time.monotonic() < deadline:
                time.sleep(0.01)
            self.assertTrue(button.value.value)
        finally:
            transport.close()


class AsyncClientTest(unittest.IsolatedAsyncioTestCase):
    async def test_async_commands_pipeline_and_preserve_results(self) -> None:
        transport, serial = open_transport()
        try:
            client = AsyncFtSwarmClient(transport)
            mode, swarm = await asyncio.gather(
                client.controller("ftSwarm400").get_microstep_mode(),
                client.swarm.get_swarm(),
            )

            self.assertEqual(mode, MicrostepMode.Sixteenth)
            self.assertEqual(swarm, "test swarm")
            self.assertEqual(
                serial.commands[-2:],
                ["ftSwarm400.getMicrostepMode()", "swarm.getSwarm(0)"],
            )
        finally:
            await client.close()

    async def test_async_subscription_changes_are_delivered(self) -> None:
        transport, serial = open_transport()
        client = AsyncFtSwarmClient(transport)
        try:
            button = await client.button("S1")
            changes = button.value.changes()
            self.assertFalse(await anext(changes))

            serial.emit("S: S1 1")
            self.assertTrue(await asyncio.wait_for(anext(changes), timeout=1))
            await changes.aclose()
        finally:
            await client.close()


if __name__ == "__main__":
    unittest.main()
