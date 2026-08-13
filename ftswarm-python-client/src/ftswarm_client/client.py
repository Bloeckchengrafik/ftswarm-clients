from __future__ import annotations

import asyncio
import logging
from collections.abc import Callable
from concurrent.futures import Future
from threading import Thread
from types import TracebackType
from typing import Any, TypeVar, cast

from .generated.objects import AsyncObjectFactories, SyncObjectFactories
from .protocol import Parser, encode_command
from .state import AsyncState, State
from .transport import SerialTransport


logger = logging.getLogger(__name__)
T = TypeVar("T")


class SyncTransactionContext:
    def __init__(self, transport: SerialTransport) -> None:
        self._transport = transport

    def command(
        self,
        target: str,
        command: str,
        parameters: tuple[Any, ...],
        parser: Parser[T],
    ) -> T:
        response = self._transport.command(encode_command(target, command, parameters)).result()
        return parser(response)

    def state(
        self,
        port: str,
        parser: Parser[T],
        initial_value: T,
        *,
        designator: str | None = None,
    ) -> State[T]:
        state = State(initial_value)
        self._transport.subscribe(port, _state_callback(state, parser, designator))
        return state


class AsyncTransactionContext:
    def __init__(self, transport: SerialTransport) -> None:
        self._transport = transport

    async def command(
        self,
        target: str,
        command: str,
        parameters: tuple[Any, ...],
        parser: Parser[T],
    ) -> T:
        future = self._transport.command(encode_command(target, command, parameters))
        response = await _await_future(future)
        return parser(response)

    def state(
        self,
        port: str,
        parser: Parser[T],
        initial_value: T,
        *,
        designator: str | None = None,
    ) -> AsyncState[T]:
        state = State(initial_value)
        self._transport.subscribe(port, _state_callback(state, parser, designator))
        return AsyncState(state)


class FtSwarmClient(SyncObjectFactories):
    @classmethod
    def open(cls, port: str | None = None, timeout: float = 5.0) -> FtSwarmClient:
        return cls(SerialTransport.open(port, timeout))

    def __init__(self, transport: SerialTransport) -> None:
        self._transport = transport
        self._context = SyncTransactionContext(transport)

    def close(self) -> None:
        self._transport.close()

    def __enter__(self) -> FtSwarmClient:
        return self

    def __exit__(
        self,
        exception_type: type[BaseException] | None,
        exception: BaseException | None,
        traceback: TracebackType | None,
    ) -> None:
        self.close()


class AsyncFtSwarmClient(AsyncObjectFactories):
    @classmethod
    async def open(cls, port: str | None = None, timeout: float = 5.0) -> AsyncFtSwarmClient:
        transport = await _call_in_thread(SerialTransport.open, port, timeout)
        return cls(transport)

    def __init__(self, transport: SerialTransport) -> None:
        self._transport = transport
        self._context = AsyncTransactionContext(transport)

    async def close(self) -> None:
        self._transport.close()

    async def __aenter__(self) -> AsyncFtSwarmClient:
        return self

    async def __aexit__(
        self,
        exception_type: type[BaseException] | None,
        exception: BaseException | None,
        traceback: TracebackType | None,
    ) -> None:
        await self.close()


def _state_callback(
    state: State[T],
    parser: Parser[T],
    designator: str | None,
) -> Callable[[str], None]:
    def update(entry: str) -> None:
        value = entry
        if designator is not None:
            actual_designator, separator, value = entry.partition(" ")
            if not separator or actual_designator != designator:
                return
        try:
            state._set(parser(value))
        except (TypeError, ValueError):
            logger.warning("Invalid subscription value for %s: %s", designator or "value", entry, exc_info=True)

    return update


async def _call_in_thread(function: Callable[..., T], *arguments: Any) -> T:
    result: Future[T] = Future()

    def run() -> None:
        try:
            result.set_result(function(*arguments))
        except BaseException as error:
            result.set_exception(error)

    Thread(target=run, name="ftswarm-async-boundary", daemon=True).start()
    return cast(T, await _await_future(result))


async def _await_future(future: Future[T]) -> T:
    while not future.done():
        await asyncio.sleep(0.001)
    return future.result()
