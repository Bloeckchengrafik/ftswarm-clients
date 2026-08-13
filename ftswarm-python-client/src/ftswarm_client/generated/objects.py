from __future__ import annotations

from collections.abc import Sequence
from enum import Enum
from typing import TYPE_CHECKING

from ftswarm_client.protocol import (
    JoystickValue,
    parse_bool,
    parse_float,
    parse_int,
    parse_joystick,
    parse_ok,
    parse_string,
)
from ftswarm_client.state import AsyncState, State
from .types import *

if TYPE_CHECKING:
    from ftswarm_client.client import AsyncTransactionContext, SyncTransactionContext

class Analog:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)


class AsyncAnalog:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

class Button:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> bool:
        return self._context.command(self._port, 'getValue', (), parse_bool)


class AsyncButton:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> bool:
        return await self._context.command(self._port, 'getValue', (), parse_bool)

class Counter:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)


class AsyncCounter:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

class Digital:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> bool:
        return self._context.command(self._port, 'getValue', (), parse_bool)


class AsyncDigital:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> bool:
        return await self._context.command(self._port, 'getValue', (), parse_bool)

class FrequencyMeter:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)


class AsyncFrequencyMeter:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

class Joystick:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[JoystickValue],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> JoystickValue:
        return self._context.command(self._port, 'getValue', (), parse_joystick)


class AsyncJoystick:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[JoystickValue],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> JoystickValue:
        return await self._context.command(self._port, 'getValue', (), parse_joystick)

class Ldr:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)


class AsyncLdr:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

class LightBarrier:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> bool:
        return self._context.command(self._port, 'getValue', (), parse_bool)


class AsyncLightBarrier:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> bool:
        return await self._context.command(self._port, 'getValue', (), parse_bool)

class Ohmmeter:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)

    def get_resistance(self) -> float:
        return self._context.command(self._port, 'getResistance', (), parse_float)


class AsyncOhmmeter:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

    async def get_resistance(self) -> float:
        return await self._context.command(self._port, 'getResistance', (), parse_float)

class Power:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)

    def get_voltage(self) -> float:
        return self._context.command(self._port, 'getVoltage', (), parse_float)


class AsyncPower:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

    async def get_voltage(self) -> float:
        return await self._context.command(self._port, 'getVoltage', (), parse_float)

class ReedSwitch:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> bool:
        return self._context.command(self._port, 'getValue', (), parse_bool)


class AsyncReedSwitch:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> bool:
        return await self._context.command(self._port, 'getValue', (), parse_bool)

class RotaryEncoder:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)


class AsyncRotaryEncoder:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

class Switch:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> bool:
        return self._context.command(self._port, 'getValue', (), parse_bool)


class AsyncSwitch:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[bool],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> bool:
        return await self._context.command(self._port, 'getValue', (), parse_bool)

class Thermometer:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)

    def get_kelvin(self) -> float:
        return self._context.command(self._port, 'getKelvin', (), parse_float)

    def get_celsius(self) -> float:
        return self._context.command(self._port, 'getCelcius', (), parse_float)

    def get_fahrenheit(self) -> float:
        return self._context.command(self._port, 'getFahrenheit', (), parse_float)


class AsyncThermometer:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

    async def get_kelvin(self) -> float:
        return await self._context.command(self._port, 'getKelvin', (), parse_float)

    async def get_celsius(self) -> float:
        return await self._context.command(self._port, 'getCelcius', (), parse_float)

    async def get_fahrenheit(self) -> float:
        return await self._context.command(self._port, 'getFahrenheit', (), parse_float)

class Voltmeter:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        value: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    def get_value(self) -> int:
        return self._context.command(self._port, 'getValue', (), parse_int)

    def get_voltage(self) -> float:
        return self._context.command(self._port, 'getVoltage', (), parse_float)


class AsyncVoltmeter:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        value: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.value = value

    async def get_value(self) -> int:
        return await self._context.command(self._port, 'getValue', (), parse_int)

    async def get_voltage(self) -> float:
        return await self._context.command(self._port, 'getVoltage', (), parse_float)

class Buzzer:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncBuzzer:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Compressor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncCompressor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Encoder:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncEncoder:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class I2c:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_register(self, register: int, value: int) -> None:
        self._context.command(self._port, 'setRegister', (register, value), parse_ok)

    def get_register(self, register: int) -> int:
        return self._context.command(self._port, 'getRegister', (register,), parse_int)


class AsyncI2c:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_register(self, register: int, value: int) -> None:
        await self._context.command(self._port, 'setRegister', (register, value), parse_ok)

    async def get_register(self, register: int) -> int:
        return await self._context.command(self._port, 'getRegister', (register,), parse_int)

class Lamp:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)

    def set_blink(self, period: int, signal: int, duty: int, pause: int, brightness1: int, brightness2: int, brightness3: int) -> None:
        self._context.command(self._port, 'setBlink', (period, signal, duty, pause, brightness1, brightness2, brightness3), parse_ok)

    def revoke_effect(self, brightness: int) -> None:
        self._context.command(self._port, 'revokeEffect', (brightness,), parse_ok)


class AsyncLamp:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

    async def set_blink(self, period: int, signal: int, duty: int, pause: int, brightness1: int, brightness2: int, brightness3: int) -> None:
        await self._context.command(self._port, 'setBlink', (period, signal, duty, pause, brightness1, brightness2, brightness3), parse_ok)

    async def revoke_effect(self, brightness: int) -> None:
        await self._context.command(self._port, 'revokeEffect', (brightness,), parse_ok)

class MMotor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncMMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class MiniMotor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncMiniMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Motor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Pixel:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_color(self, color: int) -> None:
        self._context.command(self._port, 'setColor', (color,), parse_ok)

    def get_color(self) -> str:
        return self._context.command(self._port, 'getColor', (), parse_string)

    def set_brightness(self, brightness: int) -> None:
        self._context.command(self._port, 'setBrightness', (brightness,), parse_ok)

    def get_brightness(self) -> int:
        return self._context.command(self._port, 'getBrightness', (), parse_int)

    def set_blink(self, period: int, signal: int, duty: int, pause: int, color1: int, color2: int, color3: int) -> None:
        self._context.command(self._port, 'setBlink', (period, signal, duty, pause, color1, color2, color3), parse_ok)

    def revoke_effect(self, color: int) -> None:
        self._context.command(self._port, 'revokeEffect', (color,), parse_ok)


class AsyncPixel:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_color(self, color: int) -> None:
        await self._context.command(self._port, 'setColor', (color,), parse_ok)

    async def get_color(self) -> str:
        return await self._context.command(self._port, 'getColor', (), parse_string)

    async def set_brightness(self, brightness: int) -> None:
        await self._context.command(self._port, 'setBrightness', (brightness,), parse_ok)

    async def get_brightness(self) -> int:
        return await self._context.command(self._port, 'getBrightness', (), parse_int)

    async def set_blink(self, period: int, signal: int, duty: int, pause: int, color1: int, color2: int, color3: int) -> None:
        await self._context.command(self._port, 'setBlink', (period, signal, duty, pause, color1, color2, color3), parse_ok)

    async def revoke_effect(self, color: int) -> None:
        await self._context.command(self._port, 'revokeEffect', (color,), parse_ok)

class PowerMotor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncPowerMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class RcServo:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_position(self, position: int) -> None:
        self._context.command(self._port, 'setPosition', (position,), parse_ok)

    def get_position(self) -> int:
        return self._context.command(self._port, 'getPosition', (), parse_int)

    def set_offset(self, offset: int) -> None:
        self._context.command(self._port, 'setOffset', (offset,), parse_ok)

    def get_offset(self) -> int:
        return self._context.command(self._port, 'getOffset', (), parse_int)


class AsyncRcServo:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_position(self, position: int) -> None:
        await self._context.command(self._port, 'setPosition', (position,), parse_ok)

    async def get_position(self) -> int:
        return await self._context.command(self._port, 'getPosition', (), parse_int)

    async def set_offset(self, offset: int) -> None:
        await self._context.command(self._port, 'setOffset', (offset,), parse_ok)

    async def get_offset(self) -> int:
        return await self._context.command(self._port, 'getOffset', (), parse_int)

class SMotor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncSMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Servo:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_position(self, position: int) -> None:
        self._context.command(self._port, 'setPosition', (position,), parse_ok)

    def get_position(self) -> int:
        return self._context.command(self._port, 'getPosition', (), parse_int)

    def set_offset(self, offset: int) -> None:
        self._context.command(self._port, 'setOffset', (offset,), parse_ok)

    def get_offset(self) -> int:
        return self._context.command(self._port, 'getOffset', (), parse_int)


class AsyncServo:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_position(self, position: int) -> None:
        await self._context.command(self._port, 'setPosition', (position,), parse_ok)

    async def get_position(self) -> int:
        return await self._context.command(self._port, 'getPosition', (), parse_int)

    async def set_offset(self, offset: int) -> None:
        await self._context.command(self._port, 'setOffset', (offset,), parse_ok)

    async def get_offset(self) -> int:
        return await self._context.command(self._port, 'getOffset', (), parse_int)

class StepperSubscription(Enum):
    Distance = 4
    Position = 8

class Stepper:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
        running: State[bool],
        homing: State[bool],
        distance: State[int],
        position: State[int],
    ) -> None:
        self._port = port
        self._context = context
        self.running = running
        self.homing = homing
        self.distance = distance
        self.position = position

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)

    def set_distance(self, distance: int, relative: bool = False) -> None:
        self._context.command(self._port, 'setDistance', (distance, relative), parse_ok)

    def get_distance(self) -> int:
        return self._context.command(self._port, 'getDistance', (), parse_int)

    def set_position(self, position: int) -> None:
        self._context.command(self._port, 'setPosition', (position,), parse_ok)

    def get_position(self) -> int:
        return self._context.command(self._port, 'getPosition', (), parse_int)

    def run(self) -> None:
        self._context.command(self._port, 'run', (), parse_ok)

    def is_running(self) -> bool:
        return self._context.command(self._port, 'isRunning', (), parse_bool)

    def stop(self) -> None:
        self._context.command(self._port, 'stop', (), parse_ok)

    def homing(self, max_steps: int) -> None:
        self._context.command(self._port, 'homing', (max_steps,), parse_ok)

    def is_homing(self) -> bool:
        return self._context.command(self._port, 'isHoming', (), parse_bool)

    def set_homing_offset(self, offset: int) -> None:
        self._context.command(self._port, 'setHomingOffset', (offset,), parse_ok)


class AsyncStepper:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
        running: AsyncState[bool],
        homing: AsyncState[bool],
        distance: AsyncState[int],
        position: AsyncState[int],
    ) -> None:
        self._port = port
        self._context = context
        self.running = running
        self.homing = homing
        self.distance = distance
        self.position = position

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

    async def set_distance(self, distance: int, relative: bool = False) -> None:
        await self._context.command(self._port, 'setDistance', (distance, relative), parse_ok)

    async def get_distance(self) -> int:
        return await self._context.command(self._port, 'getDistance', (), parse_int)

    async def set_position(self, position: int) -> None:
        await self._context.command(self._port, 'setPosition', (position,), parse_ok)

    async def get_position(self) -> int:
        return await self._context.command(self._port, 'getPosition', (), parse_int)

    async def run(self) -> None:
        await self._context.command(self._port, 'run', (), parse_ok)

    async def is_running(self) -> bool:
        return await self._context.command(self._port, 'isRunning', (), parse_bool)

    async def stop(self) -> None:
        await self._context.command(self._port, 'stop', (), parse_ok)

    async def homing(self, max_steps: int) -> None:
        await self._context.command(self._port, 'homing', (max_steps,), parse_ok)

    async def is_homing(self) -> bool:
        return await self._context.command(self._port, 'isHoming', (), parse_bool)

    async def set_homing_offset(self, offset: int) -> None:
        await self._context.command(self._port, 'setHomingOffset', (offset,), parse_ok)

class Tractor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncTractor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Valve:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncValve:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class WheelDrive:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncWheelDrive:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class XmMotor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncXmMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class XsMotor:
    def __init__(
        self,
        port: str,
        context: SyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    def set_speed(self, speed: int) -> None:
        self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    def get_speed(self) -> int:
        return self._context.command(self._port, 'getSpeed', (), parse_int)

    def set_motion_type(self, motion_type: int) -> None:
        self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    def get_motion_type(self) -> int:
        return self._context.command(self._port, 'getMotionType', (), parse_int)


class AsyncXsMotor:
    def __init__(
        self,
        port: str,
        context: AsyncTransactionContext,
    ) -> None:
        self._port = port
        self._context = context

    async def set_speed(self, speed: int) -> None:
        await self._context.command(self._port, 'setSpeed', (speed,), parse_ok)

    async def get_speed(self) -> int:
        return await self._context.command(self._port, 'getSpeed', (), parse_int)

    async def set_motion_type(self, motion_type: int) -> None:
        await self._context.command(self._port, 'setMotionType', (motion_type,), parse_ok)

    async def get_motion_type(self) -> int:
        return await self._context.command(self._port, 'getMotionType', (), parse_int)

class Controller:
    def __init__(self, target: str, context: SyncTransactionContext) -> None:
        self._target = target
        self._context = context

    def set_microstep_mode(self, mode: MicrostepMode) -> None:
        self._context.command(self._target, 'setMicrostepMode', (mode.wire_value,), parse_ok)

    def get_microstep_mode(self) -> MicrostepMode:
        return self._context.command(self._target, 'getMicrostepMode', (), MicrostepMode.from_wire_value)

    def reboot(self) -> None:
        self._context.command(self._target, 'reboot', (), parse_ok)

    def unsubscribe(self) -> None:
        self._context.command(self._target, 'unsubscribe', (), parse_ok)

class AsyncController:
    def __init__(self, target: str, context: AsyncTransactionContext) -> None:
        self._target = target
        self._context = context

    async def set_microstep_mode(self, mode: MicrostepMode) -> None:
        await self._context.command(self._target, 'setMicrostepMode', (mode.wire_value,), parse_ok)

    async def get_microstep_mode(self) -> MicrostepMode:
        return await self._context.command(self._target, 'getMicrostepMode', (), MicrostepMode.from_wire_value)

    async def reboot(self) -> None:
        await self._context.command(self._target, 'reboot', (), parse_ok)

    async def unsubscribe(self) -> None:
        await self._context.command(self._target, 'unsubscribe', (), parse_ok)

class Swarm:
    def __init__(self, target: str, context: SyncTransactionContext) -> None:
        self._target = target
        self._context = context

    def login(self, pin: int) -> None:
        self._context.command(self._target, 'login', (pin,), parse_ok)

    def get_swarm(self) -> str:
        return self._context.command(self._target, 'getSwarm', (0,), parse_string)

    def use_config(self, config: int) -> None:
        self._context.command(self._target, 'useConfig', (config,), parse_ok)

    def unsubscribe(self) -> None:
        self._context.command(self._target, 'unsubscribe', (), parse_ok)

class AsyncSwarm:
    def __init__(self, target: str, context: AsyncTransactionContext) -> None:
        self._target = target
        self._context = context

    async def login(self, pin: int) -> None:
        await self._context.command(self._target, 'login', (pin,), parse_ok)

    async def get_swarm(self) -> str:
        return await self._context.command(self._target, 'getSwarm', (0,), parse_string)

    async def use_config(self, config: int) -> None:
        await self._context.command(self._target, 'useConfig', (config,), parse_ok)

    async def unsubscribe(self) -> None:
        await self._context.command(self._target, 'unsubscribe', (), parse_ok)

class SyncObjectFactories:
    _context: SyncTransactionContext

    def analog(self, port: str, normally_open: bool = False) -> Analog:
        self._context.command(port, 'setIOType', (SWOSIO_ANALOG, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Analog(port, self._context, value)

    def button(self, port: str) -> Button:
        value_initial = self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Button(port, self._context, value)

    def counter(self, port: str, normally_open: bool = False) -> Counter:
        self._context.command(port, 'setIOType', (SWOSIO_COUNTER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Counter(port, self._context, value)

    def digital(self, port: str, normally_open: bool = False) -> Digital:
        self._context.command(port, 'setIOType', (SWOSIO_DIGITAL, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Digital(port, self._context, value)

    def frequency_meter(self, port: str, normally_open: bool = False) -> FrequencyMeter:
        self._context.command(port, 'setIOType', (SWOSIO_FREQUENCYMETER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return FrequencyMeter(port, self._context, value)

    def joystick(self, port: str) -> Joystick:
        value_initial = self._context.command(port, 'getValue', (), parse_joystick)
        value = self._context.state(port, parse_joystick, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Joystick(port, self._context, value)

    def ldr(self, port: str, normally_open: bool = False) -> Ldr:
        self._context.command(port, 'setIOType', (SWOSIO_LDR, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Ldr(port, self._context, value)

    def light_barrier(self, port: str, normally_open: bool = False) -> LightBarrier:
        self._context.command(port, 'setIOType', (SWOSIO_LIGHTBARRIER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return LightBarrier(port, self._context, value)

    def ohmmeter(self, port: str, normally_open: bool = False) -> Ohmmeter:
        self._context.command(port, 'setIOType', (SWOSIO_OHMMETER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Ohmmeter(port, self._context, value)

    def power(self, port: str, normally_open: bool = False) -> Power:
        self._context.command(port, 'setIOType', (SWOSIO_POWER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Power(port, self._context, value)

    def reed_switch(self, port: str, normally_open: bool = False) -> ReedSwitch:
        self._context.command(port, 'setIOType', (SWOSIO_REEDSWITCH, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return ReedSwitch(port, self._context, value)

    def rotary_encoder(self, port: str, normally_open: bool = False) -> RotaryEncoder:
        self._context.command(port, 'setIOType', (SWOSIO_ROTARYENCODER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return RotaryEncoder(port, self._context, value)

    def switch(self, port: str, normally_open: bool = False) -> Switch:
        self._context.command(port, 'setIOType', (SWOSIO_SWITCH, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Switch(port, self._context, value)

    def thermometer(self, port: str, normally_open: bool = False) -> Thermometer:
        self._context.command(port, 'setIOType', (SWOSIO_THERMOMETER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Thermometer(port, self._context, value)

    def voltmeter(self, port: str, normally_open: bool = False) -> Voltmeter:
        self._context.command(port, 'setIOType', (SWOSIO_VOLTMETER, normally_open,), parse_ok)
        value_initial = self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        self._context.command(port, 'subscribe', (), parse_ok)
        return Voltmeter(port, self._context, value)

    def buzzer(self, port: str) -> Buzzer:
        self._context.command(port, 'setIOType', (SWOSIO_BUZZER,), parse_ok)
        return Buzzer(port, self._context)

    def compressor(self, port: str) -> Compressor:
        self._context.command(port, 'setIOType', (SWOSIO_COMPRESSOR,), parse_ok)
        return Compressor(port, self._context)

    def encoder(self, port: str) -> Encoder:
        self._context.command(port, 'setIOType', (SWOSIO_ENCODER,), parse_ok)
        return Encoder(port, self._context)

    def i2c(self, port: str) -> I2c:
        return I2c(port, self._context)

    def lamp(self, port: str) -> Lamp:
        self._context.command(port, 'setIOType', (SWOSIO_LAMP,), parse_ok)
        return Lamp(port, self._context)

    def m_motor(self, port: str) -> MMotor:
        self._context.command(port, 'setIOType', (SWOSIO_MMOTOR,), parse_ok)
        return MMotor(port, self._context)

    def mini_motor(self, port: str) -> MiniMotor:
        self._context.command(port, 'setIOType', (SWOSIO_MINIMOTOR,), parse_ok)
        return MiniMotor(port, self._context)

    def motor(self, port: str) -> Motor:
        self._context.command(port, 'setIOType', (SWOSIO_MOTOR,), parse_ok)
        return Motor(port, self._context)

    def pixel(self, port: str) -> Pixel:
        return Pixel(port, self._context)

    def power_motor(self, port: str) -> PowerMotor:
        self._context.command(port, 'setIOType', (SWOSIO_POWERMOTOR,), parse_ok)
        return PowerMotor(port, self._context)

    def rc_servo(self, port: str) -> RcServo:
        return RcServo(port, self._context)

    def s_motor(self, port: str) -> SMotor:
        self._context.command(port, 'setIOType', (SWOSIO_SMOTOR,), parse_ok)
        return SMotor(port, self._context)

    def servo(self, port: str) -> Servo:
        return Servo(port, self._context)

    def stepper(self, port: str, subscriptions: Sequence[StepperSubscription] = (StepperSubscription.Position,)) -> Stepper:
        self._context.command(port, 'setIOType', (SWOSIO_STEPPER,), parse_ok)
        running_initial = self._context.command(port, 'isRunning', (), parse_bool)
        running = self._context.state(port, parse_bool, running_initial, designator='running')
        homing_initial = self._context.command(port, 'isHoming', (), parse_bool)
        homing = self._context.state(port, parse_bool, homing_initial, designator='homing')
        distance_initial = self._context.command(port, 'getDistance', (), parse_int)
        distance = self._context.state(port, parse_int, distance_initial, designator='distance')
        position_initial = self._context.command(port, 'getPosition', (), parse_int)
        position = self._context.state(port, parse_int, position_initial, designator='position')
        subscription_mask = 3
        for subscription in subscriptions:
            subscription_mask |= subscription.value
        self._context.command(port, 'subscribe', (subscription_mask,), parse_ok)
        return Stepper(port, self._context, running, homing, distance, position)

    def tractor(self, port: str) -> Tractor:
        self._context.command(port, 'setIOType', (SWOSIO_TRACTOR,), parse_ok)
        return Tractor(port, self._context)

    def valve(self, port: str) -> Valve:
        self._context.command(port, 'setIOType', (SWOSIO_VALVE,), parse_ok)
        return Valve(port, self._context)

    def wheel_drive(self, port: str) -> WheelDrive:
        self._context.command(port, 'setIOType', (SWOSIO_WHEELDRIVE,), parse_ok)
        return WheelDrive(port, self._context)

    def xm_motor(self, port: str) -> XmMotor:
        self._context.command(port, 'setIOType', (SWOSIO_XMMOTOR,), parse_ok)
        return XmMotor(port, self._context)

    def xs_motor(self, port: str) -> XsMotor:
        self._context.command(port, 'setIOType', (SWOSIO_XSMOTOR,), parse_ok)
        return XsMotor(port, self._context)

    def controller(self, name: str) -> Controller:
        return Controller(name, self._context)

    @property
    def swarm(self) -> Swarm:
        return Swarm('swarm', self._context)

class AsyncObjectFactories:
    _context: AsyncTransactionContext

    async def analog(self, port: str, normally_open: bool = False) -> AsyncAnalog:
        await self._context.command(port, 'setIOType', (SWOSIO_ANALOG, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncAnalog(port, self._context, value)

    async def button(self, port: str) -> AsyncButton:
        value_initial = await self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncButton(port, self._context, value)

    async def counter(self, port: str, normally_open: bool = False) -> AsyncCounter:
        await self._context.command(port, 'setIOType', (SWOSIO_COUNTER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncCounter(port, self._context, value)

    async def digital(self, port: str, normally_open: bool = False) -> AsyncDigital:
        await self._context.command(port, 'setIOType', (SWOSIO_DIGITAL, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncDigital(port, self._context, value)

    async def frequency_meter(self, port: str, normally_open: bool = False) -> AsyncFrequencyMeter:
        await self._context.command(port, 'setIOType', (SWOSIO_FREQUENCYMETER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncFrequencyMeter(port, self._context, value)

    async def joystick(self, port: str) -> AsyncJoystick:
        value_initial = await self._context.command(port, 'getValue', (), parse_joystick)
        value = self._context.state(port, parse_joystick, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncJoystick(port, self._context, value)

    async def ldr(self, port: str, normally_open: bool = False) -> AsyncLdr:
        await self._context.command(port, 'setIOType', (SWOSIO_LDR, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncLdr(port, self._context, value)

    async def light_barrier(self, port: str, normally_open: bool = False) -> AsyncLightBarrier:
        await self._context.command(port, 'setIOType', (SWOSIO_LIGHTBARRIER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncLightBarrier(port, self._context, value)

    async def ohmmeter(self, port: str, normally_open: bool = False) -> AsyncOhmmeter:
        await self._context.command(port, 'setIOType', (SWOSIO_OHMMETER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncOhmmeter(port, self._context, value)

    async def power(self, port: str, normally_open: bool = False) -> AsyncPower:
        await self._context.command(port, 'setIOType', (SWOSIO_POWER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncPower(port, self._context, value)

    async def reed_switch(self, port: str, normally_open: bool = False) -> AsyncReedSwitch:
        await self._context.command(port, 'setIOType', (SWOSIO_REEDSWITCH, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncReedSwitch(port, self._context, value)

    async def rotary_encoder(self, port: str, normally_open: bool = False) -> AsyncRotaryEncoder:
        await self._context.command(port, 'setIOType', (SWOSIO_ROTARYENCODER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncRotaryEncoder(port, self._context, value)

    async def switch(self, port: str, normally_open: bool = False) -> AsyncSwitch:
        await self._context.command(port, 'setIOType', (SWOSIO_SWITCH, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_bool)
        value = self._context.state(port, parse_bool, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncSwitch(port, self._context, value)

    async def thermometer(self, port: str, normally_open: bool = False) -> AsyncThermometer:
        await self._context.command(port, 'setIOType', (SWOSIO_THERMOMETER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncThermometer(port, self._context, value)

    async def voltmeter(self, port: str, normally_open: bool = False) -> AsyncVoltmeter:
        await self._context.command(port, 'setIOType', (SWOSIO_VOLTMETER, normally_open,), parse_ok)
        value_initial = await self._context.command(port, 'getValue', (), parse_int)
        value = self._context.state(port, parse_int, value_initial)
        await self._context.command(port, 'subscribe', (), parse_ok)
        return AsyncVoltmeter(port, self._context, value)

    async def buzzer(self, port: str) -> AsyncBuzzer:
        await self._context.command(port, 'setIOType', (SWOSIO_BUZZER,), parse_ok)
        return AsyncBuzzer(port, self._context)

    async def compressor(self, port: str) -> AsyncCompressor:
        await self._context.command(port, 'setIOType', (SWOSIO_COMPRESSOR,), parse_ok)
        return AsyncCompressor(port, self._context)

    async def encoder(self, port: str) -> AsyncEncoder:
        await self._context.command(port, 'setIOType', (SWOSIO_ENCODER,), parse_ok)
        return AsyncEncoder(port, self._context)

    async def i2c(self, port: str) -> AsyncI2c:
        return AsyncI2c(port, self._context)

    async def lamp(self, port: str) -> AsyncLamp:
        await self._context.command(port, 'setIOType', (SWOSIO_LAMP,), parse_ok)
        return AsyncLamp(port, self._context)

    async def m_motor(self, port: str) -> AsyncMMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_MMOTOR,), parse_ok)
        return AsyncMMotor(port, self._context)

    async def mini_motor(self, port: str) -> AsyncMiniMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_MINIMOTOR,), parse_ok)
        return AsyncMiniMotor(port, self._context)

    async def motor(self, port: str) -> AsyncMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_MOTOR,), parse_ok)
        return AsyncMotor(port, self._context)

    async def pixel(self, port: str) -> AsyncPixel:
        return AsyncPixel(port, self._context)

    async def power_motor(self, port: str) -> AsyncPowerMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_POWERMOTOR,), parse_ok)
        return AsyncPowerMotor(port, self._context)

    async def rc_servo(self, port: str) -> AsyncRcServo:
        return AsyncRcServo(port, self._context)

    async def s_motor(self, port: str) -> AsyncSMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_SMOTOR,), parse_ok)
        return AsyncSMotor(port, self._context)

    async def servo(self, port: str) -> AsyncServo:
        return AsyncServo(port, self._context)

    async def stepper(self, port: str, subscriptions: Sequence[StepperSubscription] = (StepperSubscription.Position,)) -> AsyncStepper:
        await self._context.command(port, 'setIOType', (SWOSIO_STEPPER,), parse_ok)
        running_initial = await self._context.command(port, 'isRunning', (), parse_bool)
        running = self._context.state(port, parse_bool, running_initial, designator='running')
        homing_initial = await self._context.command(port, 'isHoming', (), parse_bool)
        homing = self._context.state(port, parse_bool, homing_initial, designator='homing')
        distance_initial = await self._context.command(port, 'getDistance', (), parse_int)
        distance = self._context.state(port, parse_int, distance_initial, designator='distance')
        position_initial = await self._context.command(port, 'getPosition', (), parse_int)
        position = self._context.state(port, parse_int, position_initial, designator='position')
        subscription_mask = 3
        for subscription in subscriptions:
            subscription_mask |= subscription.value
        await self._context.command(port, 'subscribe', (subscription_mask,), parse_ok)
        return AsyncStepper(port, self._context, running, homing, distance, position)

    async def tractor(self, port: str) -> AsyncTractor:
        await self._context.command(port, 'setIOType', (SWOSIO_TRACTOR,), parse_ok)
        return AsyncTractor(port, self._context)

    async def valve(self, port: str) -> AsyncValve:
        await self._context.command(port, 'setIOType', (SWOSIO_VALVE,), parse_ok)
        return AsyncValve(port, self._context)

    async def wheel_drive(self, port: str) -> AsyncWheelDrive:
        await self._context.command(port, 'setIOType', (SWOSIO_WHEELDRIVE,), parse_ok)
        return AsyncWheelDrive(port, self._context)

    async def xm_motor(self, port: str) -> AsyncXmMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_XMMOTOR,), parse_ok)
        return AsyncXmMotor(port, self._context)

    async def xs_motor(self, port: str) -> AsyncXsMotor:
        await self._context.command(port, 'setIOType', (SWOSIO_XSMOTOR,), parse_ok)
        return AsyncXsMotor(port, self._context)

    def controller(self, name: str) -> AsyncController:
        return AsyncController(name, self._context)

    @property
    def swarm(self) -> AsyncSwarm:
        return AsyncSwarm('swarm', self._context)
