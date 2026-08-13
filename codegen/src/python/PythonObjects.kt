package io.github.elektrofuzzis.ftswarm_clients.codegen.python

import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ApiType
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.BitsetEntry
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.FunctionDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.InitStep
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.IoDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.MetaDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ParameterDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.SubscriptionDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.SubscriptionEnablement

internal fun renderPythonObjects(
    ioDefinitions: List<IoDefinition>,
    metaDefinitions: List<MetaDefinition>,
): String = buildString {
    appendLine("from __future__ import annotations")
    appendLine()
    appendLine("from collections.abc import Sequence")
    appendLine("from enum import Enum")
    appendLine("from typing import TYPE_CHECKING")
    appendLine()
    appendLine("from ftswarm_client.protocol import (")
    appendLine("    JoystickValue,")
    appendLine("    parse_bool,")
    appendLine("    parse_float,")
    appendLine("    parse_int,")
    appendLine("    parse_joystick,")
    appendLine("    parse_ok,")
    appendLine("    parse_string,")
    appendLine(")")
    appendLine("from ftswarm_client.state import AsyncState, State")
    appendLine("from .types import *")
    appendLine()
    appendLine("if TYPE_CHECKING:")
    appendLine("    from ftswarm_client.client import AsyncTransactionContext, SyncTransactionContext")
    appendLine()

    ioDefinitions.forEach { definition ->
        appendIoDefinition(definition)
        appendLine()
    }
    metaDefinitions.forEach { definition ->
        appendMetaDefinition(definition)
        appendLine()
    }
    appendSyncFactories(ioDefinitions, metaDefinitions)
    appendLine()
    appendAsyncFactories(ioDefinitions, metaDefinitions)
}

private fun StringBuilder.appendIoDefinition(definition: IoDefinition) {
    val typeName = definition.name.toPascalCase()
    val subscriptionEntries = definition.subscriptionEntries()

    if (definition.hasSelectableSubscriptions()) {
        val entries = (definition.subscription as SubscriptionDefinition.Bitset).entries
            .filter { it.enabled != SubscriptionEnablement.Always }
        appendLine("class ${typeName}Subscription(Enum):")
        entries.forEach { appendLine("    ${it.name.toPascalCase()} = ${it.mask}") }
        appendLine()
    }

    appendLine("class $typeName:")
    appendObjectConstructor(typeName, subscriptionEntries, async = false)
    definition.members.forEach { (name, function) ->
        appendLine()
        appendObjectMethod(name, function, async = false)
    }
    if (subscriptionEntries.isEmpty() && definition.members.isEmpty()) appendLine("    pass")
    appendLine()
    appendLine()
    appendLine("class Async$typeName:")
    appendObjectConstructor(typeName, subscriptionEntries, async = true)
    definition.members.forEach { (name, function) ->
        appendLine()
        appendObjectMethod(name, function, async = true)
    }
    if (subscriptionEntries.isEmpty() && definition.members.isEmpty()) appendLine("    pass")
}

private fun StringBuilder.appendObjectConstructor(
    typeName: String,
    entries: List<SubscriptionEntry>,
    async: Boolean,
) {
    val statePrefix = if (async) "Async" else ""
    val contextPrefix = if (async) "Async" else "Sync"
    appendLine("    def __init__(")
    appendLine("        self,")
    appendLine("        port: str,")
    appendLine("        context: ${contextPrefix}TransactionContext,")
    entries.forEach { entry ->
        appendLine("        ${entry.name}: ${statePrefix}State[${entry.type.pythonType()}],")
    }
    appendLine("    ) -> None:")
    appendLine("        self._port = port")
    appendLine("        self._context = context")
    entries.forEach { appendLine("        self.${it.name} = ${it.name}") }
}

private fun StringBuilder.appendObjectMethod(
    memberName: String,
    function: FunctionDefinition,
    async: Boolean,
) {
    val keyword = if (async) "async " else ""
    val await = if (async) "await " else ""
    append("    ${keyword}def ${memberName.toSnakeCase()}(self")
    function.parameters.forEach { parameter ->
        append(", ${parameter.declaration(includeDefault = true)}")
    }
    appendLine(") -> ${function.returnType.pythonType()}:")
    append("        ")
    if (function.returnType != ApiType.Ok) append("return ")
    append(await)
    append("self._context.command(self._port, ${repr(function.command)}, ")
    append(function.wireArguments())
    append(", ${function.returnType.parserExpression()})")
    appendLine()
}

private fun StringBuilder.appendMetaDefinition(definition: MetaDefinition) {
    val typeName = definition.name.toPascalCase()
    listOf(false, true).forEachIndexed { index, async ->
        val prefix = if (async) "Async" else ""
        val contextPrefix = if (async) "Async" else "Sync"
        appendLine("class $prefix$typeName:")
        appendLine("    def __init__(self, target: str, context: ${contextPrefix}TransactionContext) -> None:")
        appendLine("        self._target = target")
        appendLine("        self._context = context")
        definition.members.forEach { (name, function) ->
            appendLine()
            val keyword = if (async) "async " else ""
            val await = if (async) "await " else ""
            append("    ${keyword}def ${name.toSnakeCase()}(self")
            function.parameters.forEach { append(", ${it.declaration(includeDefault = true)}") }
            appendLine(") -> ${function.returnType.pythonType()}:")
            append("        ")
            if (function.returnType != ApiType.Ok) append("return ")
            append(await)
            append("self._context.command(self._target, ${repr(function.command)}, ")
            append(function.wireArguments())
            append(", ${function.returnType.parserExpression()})")
            appendLine()
        }
        if (index == 0) appendLine()
    }
}

private fun StringBuilder.appendSyncFactories(
    ioDefinitions: List<IoDefinition>,
    metaDefinitions: List<MetaDefinition>,
) {
    appendLine("class SyncObjectFactories:")
    appendLine("    _context: SyncTransactionContext")
    ioDefinitions.forEach { definition ->
        appendLine()
        appendFactory(definition, async = false)
    }
    appendMetaFactories(metaDefinitions, async = false)
}

private fun StringBuilder.appendAsyncFactories(
    ioDefinitions: List<IoDefinition>,
    metaDefinitions: List<MetaDefinition>,
) {
    appendLine("class AsyncObjectFactories:")
    appendLine("    _context: AsyncTransactionContext")
    ioDefinitions.forEach { definition ->
        appendLine()
        appendFactory(definition, async = true)
    }
    appendMetaFactories(metaDefinitions, async = true)
}

private fun StringBuilder.appendFactory(definition: IoDefinition, async: Boolean) {
    val typeName = definition.name.toPascalCase()
    val prefix = if (async) "Async" else ""
    val keyword = if (async) "async " else ""
    val await = if (async) "await " else ""
    val parameters = definition.initParameters()
    append("    ${keyword}def ${definition.name.toSnakeCase()}(self, port: str")
    parameters.forEach { append(", ${it.declaration(includeDefault = true)}") }
    val bitset = definition.subscription as? SubscriptionDefinition.Bitset
    if (definition.hasSelectableSubscriptions()) {
        val defaults = bitset!!.entries
            .filter { it.enabled == SubscriptionEnablement.Default }
            .joinToString(", ") { "${typeName}Subscription.${it.name.toPascalCase()}" }
        val tuple = when (defaults.count { it == ',' }) {
            0 -> if (defaults.isEmpty()) "()" else "($defaults,)"
            else -> "($defaults)"
        }
        append(", subscriptions: Sequence[${typeName}Subscription] = $tuple")
    }
    appendLine(") -> $prefix$typeName:")

    definition.init.filterIsInstance<InitStep.SetIoType>().forEach { step ->
        val arguments = buildList {
            add(step.type)
            addAll(step.parameters.map(ParameterDefinition::wireValue))
        }.joinToString(", ")
        appendLine("        ${await}self._context.command(port, 'setIOType', ($arguments,), parse_ok)")
    }

    when (val subscription = definition.subscription) {
        is SubscriptionDefinition.Value -> {
            val name = subscriptionPropertyName(subscription.initialValue)
            appendLine("        ${name}_initial = ${await}self._context.command(port, ${repr(subscription.initialValue.command)}, ${subscription.initialValue.wireArguments()}, ${subscription.parser.parserExpression()})")
            appendLine("        $name = self._context.state(port, ${subscription.parser.parserExpression()}, ${name}_initial)")
            appendLine("        ${await}self._context.command(port, 'subscribe', (), parse_ok)")
        }
        is SubscriptionDefinition.Bitset -> {
            subscription.entries.forEach { entry -> appendBitsetState(definition, entry, async) }
            val alwaysMask = subscription.entries
                .filter { it.enabled == SubscriptionEnablement.Always }
                .fold(0) { mask, entry -> mask or entry.mask }
            if (definition.hasSelectableSubscriptions()) {
                appendLine("        subscription_mask = $alwaysMask")
                appendLine("        for subscription in subscriptions:")
                appendLine("            subscription_mask |= subscription.value")
            } else {
                appendLine("        subscription_mask = $alwaysMask")
            }
            appendLine("        ${await}self._context.command(port, 'subscribe', (subscription_mask,), parse_ok)")
        }
        null -> Unit
    }

    append("        return $prefix$typeName(port, self._context")
    definition.subscriptionEntries().forEach { append(", ${it.name}") }
    appendLine(")")
}

private fun StringBuilder.appendBitsetState(
    definition: IoDefinition,
    entry: BitsetEntry,
    async: Boolean,
) {
    val await = if (async) "await " else ""
    val function = entry.initialValue
    appendLine("        ${entry.name}_initial = ${await}self._context.command(port, ${repr(function.command)}, ${function.wireArguments()}, ${entry.parser.parserExpression()})")
    appendLine("        ${entry.name} = self._context.state(port, ${entry.parser.parserExpression()}, ${entry.name}_initial, designator=${repr(entry.name)})")
}

private fun StringBuilder.appendMetaFactories(definitions: List<MetaDefinition>, async: Boolean) {
    val prefix = if (async) "Async" else ""
    definitions.forEach { definition ->
        val typeName = definition.name.toPascalCase()
        appendLine()
        when (definition.name) {
            "controller" -> {
                appendLine("    def controller(self, name: str) -> $prefix$typeName:")
                appendLine("        return $prefix$typeName(name, self._context)")
            }
            "swarm" -> {
                appendLine("    @property")
                appendLine("    def swarm(self) -> $prefix$typeName:")
                appendLine("        return $prefix$typeName('swarm', self._context)")
            }
        }
    }
}

private fun ParameterDefinition.declaration(includeDefault: Boolean): String = buildString {
    append("${name.toSnakeCase()}: ${type.pythonType()}")
    if (includeDefault) renderDefault()?.let { append(" = $it") }
}

private fun FunctionDefinition.wireArguments(): String =
    (parameters.map(ParameterDefinition::wireValue) + constantParameters.map { it.wireValue() })
        .let { arguments ->
            when (arguments.size) {
                0 -> "()"
                1 -> "(${arguments.single()},)"
                else -> arguments.joinToString(prefix = "(", postfix = ")")
            }
        }

private data class SubscriptionEntry(val name: String, val type: ApiType)

private fun IoDefinition.subscriptionEntries(): List<SubscriptionEntry> = when (val value = subscription) {
    is SubscriptionDefinition.Value -> listOf(
        SubscriptionEntry(subscriptionPropertyName(value.initialValue), value.parser)
    )
    is SubscriptionDefinition.Bitset -> value.entries.map { SubscriptionEntry(it.name.toSnakeCase(), it.parser) }
    null -> emptyList()
}

private fun subscriptionPropertyName(function: FunctionDefinition): String = when {
    function.command.startsWith("get") && function.command.length > 3 ->
        function.command.drop(3).replaceFirstChar(Char::lowercase).toSnakeCase()
    function.command.startsWith("is") && function.command.length > 2 ->
        function.command.drop(2).replaceFirstChar(Char::lowercase).toSnakeCase()
    else -> function.command.toSnakeCase()
}

private fun IoDefinition.initParameters(): List<ParameterDefinition> =
    init.filterIsInstance<InitStep.SetIoType>().flatMap { it.parameters }

internal fun IoDefinition.hasSelectableSubscriptions(): Boolean =
    (subscription as? SubscriptionDefinition.Bitset)
        ?.entries
        ?.any { it.enabled != SubscriptionEnablement.Always } == true
