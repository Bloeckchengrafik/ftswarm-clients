from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Callable, TypeVar


class FtSwarmError(Exception):
    pass


@dataclass(frozen=True)
class JoystickValue:
    lr: float
    fb: float


T = TypeVar("T")
Parser = Callable[[str], T]


def encode_command(target: str, command: str, parameters: tuple[Any, ...]) -> str:
    encoded = ",".join(_encode_parameter(parameter) for parameter in parameters)
    return f"{target}.{command}({encoded})"


def parse_ok(value: str) -> None:
    if " ok" not in value.lower():
        raise ValueError(f"Invalid OK response: {value}")
    sequence, _, _ = value.partition(" ")
    int(sequence)


def parse_int(value: str) -> int:
    return int(value)


def parse_float(value: str) -> float:
    return float(value)


def parse_bool(value: str) -> bool:
    if value == "1":
        return True
    if value == "0":
        return False
    raise ValueError(f"Invalid boolean response: {value}")


def parse_string(value: str) -> str:
    if len(value) >= 2 and value[0] == value[-1] == '"':
        return value[1:-1]
    return value


def parse_joystick(value: str) -> JoystickValue:
    left_right, forward_back = value.split()
    return JoystickValue(float(left_right), float(forward_back))


def _encode_parameter(value: Any) -> str:
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, str):
        if '"' in value:
            raise ValueError("String command parameters cannot contain double quotes")
        return f'"{value}"'
    if isinstance(value, (int, float)):
        return str(value)
    raise TypeError(f"Unsupported command parameter: {type(value).__name__}")
