from __future__ import annotations

import asyncio
from collections.abc import AsyncIterator, Callable, Iterator
from queue import Queue
from threading import Lock
from typing import Generic, TypeVar


T = TypeVar("T")


class State(Generic[T]):
    def __init__(self, initial_value: T) -> None:
        self._value = initial_value
        self._version = 0
        self._lock = Lock()
        self._listeners: set[Callable[[T], None]] = set()

    @property
    def value(self) -> T:
        with self._lock:
            return self._value

    def changes(self) -> Iterator[T]:
        updates: Queue[T] = Queue()
        unsubscribe = self._listen(updates.put)
        try:
            updates.put(self.value)
            while True:
                yield updates.get()
        finally:
            unsubscribe()

    def _set(self, value: T) -> None:
        with self._lock:
            self._value = value
            self._version += 1
            listeners = tuple(self._listeners)
        for listener in listeners:
            listener(value)

    def _listen(self, listener: Callable[[T], None]) -> Callable[[], None]:
        with self._lock:
            self._listeners.add(listener)

        def unsubscribe() -> None:
            with self._lock:
                self._listeners.discard(listener)

        return unsubscribe

    def _snapshot(self) -> tuple[int, T]:
        with self._lock:
            return self._version, self._value


class AsyncState(Generic[T]):
    def __init__(self, state: State[T]) -> None:
        self._state = state

    @property
    def value(self) -> T:
        return self._state.value

    async def changes(self) -> AsyncIterator[T]:
        seen_version = -1
        while True:
            version, value = self._state._snapshot()
            if version != seen_version:
                seen_version = version
                yield value
            await asyncio.sleep(0.01)
