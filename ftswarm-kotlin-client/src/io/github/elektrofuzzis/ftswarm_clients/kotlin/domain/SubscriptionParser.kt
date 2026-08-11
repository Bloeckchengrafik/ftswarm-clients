package io.github.elektrofuzzis.ftswarm_clients.kotlin.domain

data class SubscriptionJoystickValue(val lr: Float, val fb: Float)

data class DesignatedValue<T>(val designator: String, val value: T)

interface SubscriptionParser<T> {
    fun parse(value: String): Result<T>

    companion object {
        val int = object : SubscriptionParser<Int> {
            override fun parse(value: String) =
                value.toIntOrNull()?.let { Result.success(it) } ?: Result.failure(
                    IllegalArgumentException("Invalid subscription value: $value")
                )
        }

        val float = object : SubscriptionParser<Float> {
            override fun parse(value: String) =
                value.toFloatOrNull()?.let { Result.success(it) } ?: Result.failure(
                    IllegalArgumentException("Invalid subscription value: $value")
                )
        }

        val string = object : SubscriptionParser<String> {
            override fun parse(value: String) = Result.success(value)
        }

        val bool = object : SubscriptionParser<Boolean> {
            override fun parse(value: String) = when (value) {
                "1" -> Result.success(true)
                "0" -> Result.success(false)
                else -> Result.failure(IllegalArgumentException("Invalid subscription value: $value"))
            }
        }

        val microstepMode = object : SubscriptionParser<MicrostepMode> {
            override fun parse(value: String) = value.toIntOrNull()
                ?.let(MicrostepMode::fromWireValue)
                ?.let(Result.Companion::success)
                ?: Result.failure(IllegalArgumentException("Invalid microstep mode: $value"))
        }

        val joystick = object : SubscriptionParser<SubscriptionJoystickValue> {
            override fun parse(value: String) =
                value.split(" ").takeIf { it.size == 2 }?.let {
                    SubscriptionJoystickValue(it[0].toFloat(), it[1].toFloat())
                }?.let { Result.success(it) } ?: Result.failure(
                    IllegalArgumentException("Invalid subscription value: $value")
                )
        }

        fun <T> designated(parser: SubscriptionParser<T>) = object : SubscriptionParser<DesignatedValue<T>> {
            override fun parse(value: String) =
                value.split(" ", limit = 2).takeIf { it.size == 2 }?.let {
                    DesignatedValue(it[0], parser.parse(it[1]).getOrThrow())
                }?.let { Result.success(it) } ?: Result.failure(
                    IllegalArgumentException("Invalid subscription value: $value")
                )
        }
    }
}
