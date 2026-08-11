package io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin

import io.github.elektrofuzzis.ftswarm_clients.codegen.CodeGenUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.ApiType
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.MetaDefinition

fun kotlinMetaObjectUnits(definitions: List<MetaDefinition>): List<CodeGenUnit> =
    definitions.map { definition ->
        val typeName = definition.typeName()
        CodeGenUnit("$typeName.kt") {
            renderMetaObject(definition, typeName)
        }
    }

private fun renderMetaObject(definition: MetaDefinition, typeName: String): String =
    buildString {
        val usesMicrostepMode = definition.members.values.any { function ->
            function.returnType == ApiType.MicrostepMode || function.parameters.any { it.type == ApiType.MicrostepMode }
        }
        appendLine("package io.github.elektrofuzzis.ftswarm_clients.kotlin.objects")
        appendLine()
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmClient")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmProtocolObject")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmTransactionContext")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.Command")
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.CommandRequest")
        if (usesMicrostepMode) {
            appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.MicrostepMode")
        }
        appendLine("import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.ReturnValueParser")
        appendLine()

        definition.members.forEach { (memberName, function) ->
            appendCommandFunction(definition.name, memberName, function, targetName = "target")
            appendLine()
        }

        appendLine("class $typeName internal constructor(")
        appendLine("    private val target: String,")
        appendLine("    private val context: FtSwarmTransactionContext,")
        appendLine(") : FtSwarmProtocolObject {")
        definition.members.forEach { (memberName, function) ->
            if (function.returnType == ApiType.Ok) {
                append("    suspend fun $memberName(")
                appendParameters(function.parameters, includeDefaults = true)
                appendLine(") {")
                append("        context.${definition.name.commandHelperName(memberName)}(target")
                function.parameters.forEach { append(", ${it.name}") }
                appendLine(")")
                appendLine("    }")
            } else {
                append("    suspend fun $memberName(")
                appendParameters(function.parameters, includeDefaults = true)
                append(") = context.${definition.name.commandHelperName(memberName)}(target")
                function.parameters.forEach { append(", ${it.name}") }
                appendLine(")")
            }
            appendLine()
        }
        if (definition.members.isNotEmpty()) setLength(length - 1)
        appendLine("}")
        appendLine()

        when (definition.name) {
            "controller" -> appendLine("fun FtSwarmClient.controller(name: String) = $typeName(name, context)")
            "swarm" -> {
                appendLine("val FtSwarmClient.swarm: $typeName")
                appendLine("    get() = $typeName(\"swarm\", context)")
            }
        }
    }.trimEnd() + "\n"

private fun MetaDefinition.typeName(): String = when (name) {
    "controller" -> "FtSwarmController"
    "swarm" -> "FtSwarm"
    else -> error("Unsupported meta definition: $name")
}
