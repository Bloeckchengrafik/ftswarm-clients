package io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin

import io.github.elektrofuzzis.ftswarm_clients.codegen.CodeGenUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getMicrostepModes

fun kotlinMicrostepModeUnit() = CodeGenUnit("MicrostepMode.kt") { apiDefPath ->
    val modes = getMicrostepModes(apiDefPath)

    buildString {
        appendLine("package io.github.elektrofuzzis.ftswarm_clients.kotlin.domain")
        appendLine()
        appendLine("enum class MicrostepMode(internal val wireValue: Int) {")
        modes.forEachIndexed { index, mode ->
            val suffix = if (index == modes.lastIndex) ";" else ","
            appendLine("    ${mode.name.toPascalCase()}(${mode.value})$suffix")
        }
        appendLine()
        appendLine("    companion object {")
        appendLine("        internal fun fromWireValue(value: Int): MicrostepMode? = entries.find { it.wireValue == value }")
        appendLine("    }")
        appendLine("}")
    }
}
