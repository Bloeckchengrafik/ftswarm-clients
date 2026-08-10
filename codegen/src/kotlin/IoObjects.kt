package io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin

import io.github.elektrofuzzis.ftswarm_clients.codegen.CodeGenUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ApiType
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.BitsetEntry
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.FunctionDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.InitStep
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.IoDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ParameterDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.SubscriptionDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.SubscriptionEnablement

fun kotlinIoObjectUnits(definitions: List<IoDefinition>): List<CodeGenUnit> =
    definitions.map { definition ->
        CodeGenUnit("FtSwarm${definition.name.toPascalCase()}.kt") {
            renderIoObject(definition)
        }
    }

private fun renderIoObject(definition: IoDefinition): String {
    val typeName = "FtSwarm${definition.name.toPascalCase()}"
    val initParameters = definition.init
        .filterIsInstance<InitStep.SetIoType>()
        .flatMap { it.parameters }
    val subscription = definition.subscription
    val usesJoystick = definition.members.values.any { it.returnType == ApiType.Joystick } ||
        (subscription is SubscriptionDefinition.Value && subscription.parser == ApiType.Joystick)

    return buildString {
        appendLine("package io.github.elektrofuzzis.ftswarm_clients.kotlin.objects")
        appendLine()
        appendLine("import github.elektrofuzzis.ftswarm_clients.kotlin.codegen.IOTypes")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmClient")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmProtocolObject")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmTransactionContext")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmTransactionContextImpl")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.Command")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.CommandRequest")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.ReturnValueParser")
        if (usesJoystick) {
            appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.SubscriptionJoystickValue")
        }
        if (subscription != null) {
            appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.SubscriptionParser")
            appendLine("import kotlinx.coroutines.flow.SharingStarted")
            appendLine("import kotlinx.coroutines.flow.StateFlow")
            appendLine("import kotlinx.coroutines.flow.stateIn")
        }
        if (subscription is SubscriptionDefinition.Bitset) {
            appendLine("import kotlinx.coroutines.flow.filter")
            appendLine("import kotlinx.coroutines.flow.mapNotNull")
        }
        appendLine()

        if (subscription is SubscriptionDefinition.Bitset) {
            appendBitsetSelection(typeName, subscription.entries)
            appendLine()
        }

        appendCreateFunction(definition, typeName, initParameters)
        appendLine()

        definition.members.forEach { (memberName, function) ->
            appendCommandFunction(definition.name, memberName, function)
            appendLine()
        }

        appendProtocolClass(definition, typeName)
        appendLine()
        appendClientFactory(definition, typeName, initParameters)
    }.trimEnd() + "\n"
}

private fun StringBuilder.appendBitsetSelection(
    typeName: String,
    entries: List<BitsetEntry>,
) {
    val selectable = entries.filter { it.enabled != SubscriptionEnablement.Always }
    appendLine("enum class ${typeName}Subscription(internal val mask: Int) {")
    selectable.forEachIndexed { index, entry ->
        val suffix = if (index == selectable.lastIndex) ";" else ","
        appendLine("    ${entry.name.toPascalCase()}(${entry.mask.toHex()})$suffix")
    }
    appendLine("}")
}

private fun StringBuilder.appendCreateFunction(
    definition: IoDefinition,
    typeName: String,
    initParameters: List<ParameterDefinition>,
) {
    val subscription = definition.subscription
    appendLine("private suspend fun create${definition.name.toPascalCase()}(")
    appendLine("    port: String,")
    appendLine("    context: FtSwarmTransactionContextImpl,")
    initParameters.forEach { appendLine("    ${it.name}: ${it.type.kotlinType()},") }
    if (subscription is SubscriptionDefinition.Bitset && subscription.entries.any { it.enabled != SubscriptionEnablement.Always }) {
        appendLine("    subscriptions: List<${typeName}Subscription>,")
    }
    appendLine("): $typeName {")
    appendLine("    val cx = context.child(\"ftSwarm/\$port\")")

    definition.init.filterIsInstance<InitStep.SetIoType>().forEach { step ->
        appendLine("    cx.command(")
        appendLine("        CommandRequest(")
        appendLine("            Command(")
        appendLine("                port,")
        appendLine("                \"setIOType\",")
        appendLine("                Command.Parameter.int(IOTypes.${step.type}),")
        step.parameters.forEach { parameter ->
            appendLine("                ${parameter.commandParameter(parameter.name)},")
        }
        appendLine("            ),")
        appendLine("            ReturnValueParser.Ok,")
        appendLine("        )")
        appendLine("    ).getOrThrow()")
    }

    when (subscription) {
        is SubscriptionDefinition.Value -> appendValueSubscription(definition, subscription)
        is SubscriptionDefinition.Bitset -> appendBitsetSubscription(definition, typeName, subscription.entries)
        null -> Unit
    }

    append("    return $typeName(port, cx")
    subscriptionEntries(definition).forEach { entry -> append(", ${entry.name}") }
    appendLine(")")
    appendLine("}")
}

private fun StringBuilder.appendValueSubscription(
    definition: IoDefinition,
    subscription: SubscriptionDefinition.Value,
) {
    val property = subscriptionPropertyName(subscription.initialValue)
    appendLine("    val $property = cx.getSubscriptions(port, SubscriptionParser.${subscription.parser.subscriptionParser()})")
    appendLine("        .stateIn(")
    appendLine("            cx,")
    appendLine("            SharingStarted.Eagerly,")
    appendLine("            cx.${definition.name.commandHelperName(subscription.initialValue.command)}(port),")
    appendLine("        )")
    appendSubscribeCommand()
}

private fun StringBuilder.appendBitsetSubscription(
    definition: IoDefinition,
    typeName: String,
    entries: List<BitsetEntry>,
) {
    appendLine("    val subscriptionUpdates = cx.getSubscriptions(")
    appendLine("        port,")
    appendLine("        SubscriptionParser.designated(SubscriptionParser.string),")
    appendLine("    )")
    entries.forEach { entry ->
        appendLine("    val ${entry.name} = subscriptionUpdates")
        appendLine("        .filter { it.designator == \"${entry.name}\" }")
        appendLine("        .mapNotNull { SubscriptionParser.${entry.parser.subscriptionParser()}.parse(it.value).getOrNull() }")
        appendLine("        .stateIn(")
        appendLine("            cx,")
        appendLine("            SharingStarted.Eagerly,")
        appendLine("            cx.${definition.name.commandHelperName(entry.initialValue.command)}(port),")
        appendLine("        )")
    }

    val alwaysMask = entries
        .filter { it.enabled == SubscriptionEnablement.Always }
        .fold(0) { mask, entry -> mask or entry.mask }
    val selectable = entries.any { it.enabled != SubscriptionEnablement.Always }
    if (selectable) {
        appendLine("    val subscriptionMask = ${alwaysMask.toHex()} or")
        appendLine("        subscriptions.fold(0) { mask, subscription -> mask or subscription.mask }")
    } else {
        appendLine("    val subscriptionMask = ${alwaysMask.toHex()}")
    }
    appendSubscribeCommand("Command.Parameter.int(subscriptionMask)")
}

private fun StringBuilder.appendSubscribeCommand(parameter: String? = null) {
    appendLine("    cx.command(")
    appendLine("        CommandRequest(")
    appendLine("            Command(")
    appendLine("                port,")
    appendLine("                \"subscribe\",")
    if (parameter != null) appendLine("                $parameter,")
    appendLine("            ),")
    appendLine("            ReturnValueParser.Ok,")
    appendLine("        )")
    appendLine("    ).getOrThrow()")
}

private fun StringBuilder.appendCommandFunction(
    ioName: String,
    memberName: String,
    function: FunctionDefinition,
) {
    val helperName = ioName.commandHelperName(memberName)
    append("private suspend fun FtSwarmTransactionContext.$helperName(port: String")
    function.parameters.forEach { parameter ->
        append(", ${parameter.name}: ${parameter.type.kotlinType()}")
    }
    append(")")
    if (function.returnType != ApiType.Ok) append(": ${function.returnType.kotlinType()}")
    appendLine(" {")
    val resultPrefix = if (function.returnType == ApiType.Ok) "" else "val result = "
    appendLine("    ${resultPrefix}command(")
    appendLine("        CommandRequest(")
    appendLine("            Command(")
    appendLine("                port,")
    appendLine("                \"${function.command}\",")
    function.parameters.forEach { parameter ->
        appendLine("                ${parameter.commandParameter(parameter.name)},")
    }
    appendLine("            ),")
    appendLine("            ReturnValueParser.${function.returnType.returnValueParser()},")
    appendLine("        )")
    appendLine("    ).getOrThrow()")
    if (function.returnType != ApiType.Ok) appendLine("    return result.value")
    appendLine("}")
}

private fun StringBuilder.appendProtocolClass(
    definition: IoDefinition,
    typeName: String,
) {
    val entries = subscriptionEntries(definition)
    appendLine("class $typeName(")
    appendLine("    private val port: String,")
    appendLine("    private val context: FtSwarmTransactionContext,")
    entries.forEach { entry ->
        appendLine("    ${entry.name}State: StateFlow<${entry.type.kotlinType()}>,")
    }
    appendLine(") : FtSwarmProtocolObject {")

    entries.forEach { entry ->
        if (entry.enabled != null && entry.enabled != SubscriptionEnablement.Always) {
            appendLine("    /**")
            appendLine("     * This state remains at its initial snapshot unless")
            appendLine("     * [${typeName}Subscription.${entry.name.toPascalCase()}] is selected.")
            appendLine("     */")
        }
        appendLine("    val ${entry.name}: StateFlow<${entry.type.kotlinType()}> = ${entry.name}State")
        appendLine()
    }

    definition.members.forEach { (memberName, function) ->
        if (function.returnType == ApiType.Ok) {
            append("    suspend fun $memberName(")
            appendParameters(function.parameters, includeDefaults = true)
            appendLine(") {")
            append("        context.${definition.name.commandHelperName(memberName)}(port")
            function.parameters.forEach { append(", ${it.name}") }
            appendLine(")")
            appendLine("    }")
        } else {
            append("    suspend fun $memberName(")
            appendParameters(function.parameters, includeDefaults = true)
            append(") = context.${definition.name.commandHelperName(memberName)}(port")
            function.parameters.forEach { append(", ${it.name}") }
            appendLine(")")
        }
        appendLine()
    }

    if (definition.members.isNotEmpty() || entries.isNotEmpty()) {
        setLength(length - 1)
    }
    appendLine("}")
}

private fun StringBuilder.appendClientFactory(
    definition: IoDefinition,
    typeName: String,
    initParameters: List<ParameterDefinition>,
) {
    val subscription = definition.subscription
    append("suspend fun FtSwarmClient.${definition.name}(port: String")
    initParameters.forEach { parameter ->
        append(", ${parameter.name}: ${parameter.type.kotlinType()}")
        parameter.defaultValueText()?.let { append(" = ${parameter.renderDefault(it)}") }
    }
    if (subscription is SubscriptionDefinition.Bitset) {
        val selectable = subscription.entries.filter { it.enabled != SubscriptionEnablement.Always }
        if (selectable.isNotEmpty()) {
            val defaults = selectable
                .filter { it.enabled == SubscriptionEnablement.Default }
                .joinToString(", ") { "${typeName}Subscription.${it.name.toPascalCase()}" }
            append(", subscriptions: List<${typeName}Subscription> = listOf($defaults)")
        }
    }
    append(") = create${definition.name.toPascalCase()}(port, context")
    initParameters.forEach { append(", ${it.name}") }
    if (subscription is SubscriptionDefinition.Bitset && subscription.entries.any { it.enabled != SubscriptionEnablement.Always }) {
        append(", subscriptions")
    }
    appendLine(")")
}

private data class SubscriptionEntry(
    val name: String,
    val type: ApiType,
    val enabled: SubscriptionEnablement?,
)

private fun subscriptionEntries(definition: IoDefinition): List<SubscriptionEntry> =
    when (val subscription = definition.subscription) {
        is SubscriptionDefinition.Value -> listOf(
            SubscriptionEntry(
                subscriptionPropertyName(subscription.initialValue),
                subscription.parser,
                null,
            )
        )

        is SubscriptionDefinition.Bitset -> subscription.entries.map {
            SubscriptionEntry(it.name, it.parser, it.enabled)
        }

        null -> emptyList()
    }

private fun subscriptionPropertyName(function: FunctionDefinition): String =
    when {
        function.command.startsWith("get") && function.command.length > 3 ->
            function.command.drop(3).replaceFirstChar(Char::lowercase)

        function.command.startsWith("is") && function.command.length > 2 ->
            function.command.drop(2).replaceFirstChar(Char::lowercase)

        else -> function.command
    }

private fun StringBuilder.appendParameters(
    parameters: List<ParameterDefinition>,
    includeDefaults: Boolean,
) {
    parameters.forEachIndexed { index, parameter ->
        if (index > 0) append(", ")
        append("${parameter.name}: ${parameter.type.kotlinType()}")
        if (includeDefaults) {
            parameter.defaultValueText()?.let { append(" = ${parameter.renderDefault(it)}") }
        }
    }
}

private fun ParameterDefinition.renderDefault(value: String): String =
    when (type) {
        ApiType.String -> "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        else -> value
    }

private fun ParameterDefinition.commandParameter(value: String): String =
    "Command.Parameter.${type.commandParameter()}($value)"

private fun ApiType.kotlinType(): String = when (this) {
    ApiType.Boolean -> "Boolean"
    ApiType.Int -> "Int"
    ApiType.Float -> "Float"
    ApiType.String -> "String"
    ApiType.Joystick -> "SubscriptionJoystickValue"
    ApiType.Ok -> "Unit"
}

private fun ApiType.commandParameter(): String = when (this) {
    ApiType.Boolean -> "boolean"
    ApiType.Int -> "int"
    ApiType.Float -> "float"
    ApiType.String -> "string"
    ApiType.Joystick -> error("joystick is not a command parameter type")
    ApiType.Ok -> error("ok is not a command parameter type")
}

private fun ApiType.returnValueParser(): String = when (this) {
    ApiType.Boolean -> "BooleanValue"
    ApiType.Int -> "IntValue"
    ApiType.Float -> "FloatValue"
    ApiType.String -> "StringValue"
    ApiType.Joystick -> "JoystickValue"
    ApiType.Ok -> "Ok"
}

private fun ApiType.subscriptionParser(): String = when (this) {
    ApiType.Boolean -> "bool"
    ApiType.Int -> "int"
    ApiType.Float -> "float"
    ApiType.String -> "string"
    ApiType.Joystick -> "joystick"
    ApiType.Ok -> error("ok is not a subscription value type")
}

private fun String.commandHelperName(command: String): String =
    this + command.toPascalCase()

private fun String.toPascalCase(): String =
    split(Regex("[^A-Za-z0-9]+"))
        .filter(String::isNotEmpty)
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercase) }

private fun Int.toHex(): String = "0x${toString(16).uppercase().padStart(2, '0')}"
