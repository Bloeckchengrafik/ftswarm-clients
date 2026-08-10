package io.github.elektrofuzzis.ftswarm_clients.kotlin.domain

@JvmInline
value class Command(val command: String) {
    interface Parameter {
        fun toCommandParameter(): String

        data class IntValue(val value: Int) : Parameter {
            override fun toCommandParameter() = value.toString()
        }

        data class FloatValue(val value: Float) : Parameter {
            override fun toCommandParameter() = value.toString()
        }

        data class StringValue(val value: String) : Parameter {
            init {
                if (value.contains('"')) throw IllegalArgumentException("String values cannot contain double quotes")
            }

            override fun toCommandParameter() = "\"$value\""
        }

        data class ColorValue(val value: Int) : Parameter {
            constructor(red: Int, green: Int, blue: Int) : this(
                (red shl 16) or (green shl 8) or blue
            )

            override fun toCommandParameter() = value.toString()
        }
        companion object {
            fun int(value: Int) = IntValue(value)
            fun float(value: Float) = FloatValue(value)
            fun string(value: String) = StringValue(value)
            fun color(value: Int) = ColorValue(value)
            fun color(red: Int, green: Int, blue: Int) = ColorValue(red, green, blue)
            fun boolean(value: Boolean) = if (value) int(1) else int(0)
        }

    }

    companion object {
        operator fun invoke(port: String, command: String, vararg params: Parameter) = Command(buildString {
            append(port)
            append(".")
            append(command)
            append("(")
            params.forEachIndexed { index, param ->
                if (index > 0) append(",")
                append(param.toCommandParameter())
            }
            append(")")
        })
    }
}

data class CommandRequest<T : SucceedingCommandReturnValue>(
    val command: Command,
    val parser: ReturnValueParser<T>
) {
    override fun toString(): String = command.command
}