package io.github.elektrofuzzis.ftswarm_clients.codegen.python

import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ApiType
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ConstantParameterDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ParameterDefinition

internal fun String.toPascalCase(): String =
    split(Regex("[^A-Za-z0-9]+"))
        .filter(String::isNotEmpty)
        .joinToString("") { it.replaceFirstChar(Char::uppercase) }

internal fun String.toSnakeCase(): String =
    replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
        .lowercase()

internal fun ApiType.pythonType(): String = when (this) {
    ApiType.Boolean -> "bool"
    ApiType.Int -> "int"
    ApiType.Float -> "float"
    ApiType.String -> "str"
    ApiType.Joystick -> "JoystickValue"
    ApiType.MicrostepMode -> "MicrostepMode"
    ApiType.Ok -> "None"
}

internal fun ApiType.parserExpression(): String = when (this) {
    ApiType.Boolean -> "parse_bool"
    ApiType.Int -> "parse_int"
    ApiType.Float -> "parse_float"
    ApiType.String -> "parse_string"
    ApiType.Joystick -> "parse_joystick"
    ApiType.MicrostepMode -> "MicrostepMode.from_wire_value"
    ApiType.Ok -> "parse_ok"
}

internal fun ParameterDefinition.renderDefault(): String? = defaultValueText()?.let(type::renderLiteral)

internal fun ApiType.renderLiteral(value: String): String = when (this) {
    ApiType.Boolean -> if (value == "true") "True" else "False"
    ApiType.String -> repr(value)
    else -> value
}

internal fun ParameterDefinition.wireValue(): String = when (type) {
    ApiType.MicrostepMode -> "${name.toSnakeCase()}.wire_value"
    else -> name.toSnakeCase()
}

internal fun ConstantParameterDefinition.wireValue(): String =
    type.renderLiteral(requireNotNull(valueText()))

internal fun repr(value: String): String = "'" + value
    .replace("\\", "\\\\")
    .replace("'", "\\'") + "'"
