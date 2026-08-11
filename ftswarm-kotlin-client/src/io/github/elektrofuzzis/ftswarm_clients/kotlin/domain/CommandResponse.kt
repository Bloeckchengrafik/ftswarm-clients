package io.github.elektrofuzzis.ftswarm_clients.kotlin.domain

sealed interface SucceedingCommandReturnValue {
    data class Ok(val seq: Int) : SucceedingCommandReturnValue
    data class IntValue(val value: Int) : SucceedingCommandReturnValue
    data class BooleanValue(val value: Boolean) : SucceedingCommandReturnValue
    data class StringValue(val value: String) : SucceedingCommandReturnValue
    data class FloatValue(val value: Float) : SucceedingCommandReturnValue
    data class MicrostepModeValue(val value: MicrostepMode) : SucceedingCommandReturnValue
    data class JoystickValue(val lr: Float, val fb: Float) : SucceedingCommandReturnValue {
        val value: SubscriptionJoystickValue
            get() = SubscriptionJoystickValue(lr, fb)
    }
}

sealed interface ReturnValueParser<T : SucceedingCommandReturnValue> {
    fun parse(value: String): Result<T>

    data object Ok : ReturnValueParser<SucceedingCommandReturnValue.Ok> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.Ok> {
            if (!value.lowercase().contains(" ok")) return Result.failure(IllegalArgumentException("Invalid response: $value"))
            val parts = value.split(" ", limit = 2)
            val sequence = parts[0].toIntOrNull()
                ?: return Result.failure(IllegalArgumentException("Invalid sequence number: $value"))
            return Result.success(SucceedingCommandReturnValue.Ok(sequence))
        }
    }

    data object IntValue : ReturnValueParser<SucceedingCommandReturnValue.IntValue> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.IntValue> {
            return value.toIntOrNull()?.let { Result.success(SucceedingCommandReturnValue.IntValue(it)) }
                ?: Result.failure(IllegalArgumentException("Invalid response: $value"))
        }
    }

    data object BooleanValue : ReturnValueParser<SucceedingCommandReturnValue.BooleanValue> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.BooleanValue> {
            return when (value) {
                "1" -> Result.success(SucceedingCommandReturnValue.BooleanValue(true))
                "0" -> Result.success(SucceedingCommandReturnValue.BooleanValue(false))
                else -> Result.failure(IllegalArgumentException("Invalid response: $value"))
            }
        }
    }

    data object StringValue : ReturnValueParser<SucceedingCommandReturnValue.StringValue> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.StringValue> {
            return Result.success(SucceedingCommandReturnValue.StringValue(value.removeSurrounding("\"")))
        }
    }

    data object FloatValue : ReturnValueParser<SucceedingCommandReturnValue.FloatValue> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.FloatValue> {
            return value.toFloatOrNull()?.let { Result.success(SucceedingCommandReturnValue.FloatValue(it)) }
                ?: Result.failure(IllegalArgumentException("Invalid response: $value"))
        }
    }

    data object MicrostepModeValue : ReturnValueParser<SucceedingCommandReturnValue.MicrostepModeValue> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.MicrostepModeValue> {
            val wireValue = value.toIntOrNull()
                ?: return Result.failure(IllegalArgumentException("Invalid response: $value"))
            val mode = MicrostepMode.fromWireValue(wireValue)
                ?: return Result.failure(IllegalArgumentException("Unknown microstep mode: $wireValue"))
            return Result.success(SucceedingCommandReturnValue.MicrostepModeValue(mode))
        }
    }

    data object JoystickValue : ReturnValueParser<SucceedingCommandReturnValue.JoystickValue> {
        override fun parse(value: String): Result<SucceedingCommandReturnValue.JoystickValue> {
            return value.split(" ").takeIf { it.size == 2 }?.let {
                Result.success(SucceedingCommandReturnValue.JoystickValue(it[0].toFloat(), it[1].toFloat()))
            } ?: Result.failure(IllegalArgumentException("Invalid response: $value"))
        }
    }
}
