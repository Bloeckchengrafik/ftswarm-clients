package io.github.elektrofuzzis.ftswarm_clients.codegen.python

import io.github.elektrofuzzis.ftswarm_clients.codegen.CodeGenUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.IoDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.MetaDefinition
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getIoTypes
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getMicrostepModes
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getVendorIds

fun pythonGeneratedUnits(
    ioDefinitions: List<IoDefinition>,
    metaDefinitions: List<MetaDefinition>,
): List<CodeGenUnit> = listOf(
    CodeGenUnit("types.py") { apiDefPath ->
        val ioTypes = getIoTypes(apiDefPath)
        val modes = getMicrostepModes(apiDefPath)
        val vendorIds = getVendorIds(apiDefPath)
        buildString {
            appendLine("from __future__ import annotations")
            appendLine()
            appendLine("from enum import Enum")
            appendLine()
            ioTypes.forEach { appendLine("${it.constantName} = ${it.id}") }
            appendLine()
            appendLine("VENDOR_IDS: tuple[tuple[int, int], ...] = (")
            vendorIds.forEach { appendLine("    (${it.vid}, ${it.pid}),") }
            appendLine(")")
            appendLine()
            appendLine("class MicrostepMode(Enum):")
            modes.forEach { appendLine("    ${it.name.toPascalCase()} = ${it.value}") }
            appendLine()
            appendLine("    @property")
            appendLine("    def wire_value(self) -> int:")
            appendLine("        return int(self.value)")
            appendLine()
            appendLine("    @classmethod")
            appendLine("    def from_wire_value(cls, value: str) -> MicrostepMode:")
            appendLine("        return cls(int(value))")
        }
    },
    CodeGenUnit("objects.py") {
        renderPythonObjects(ioDefinitions, metaDefinitions)
    },
    CodeGenUnit("__init__.py") {
        renderPythonExports(ioDefinitions, metaDefinitions)
    },
)

private fun renderPythonExports(
    ioDefinitions: List<IoDefinition>,
    metaDefinitions: List<MetaDefinition>,
): String {
    val names = buildList {
        ioDefinitions.forEach { definition ->
            add(definition.name.toPascalCase())
            add("Async${definition.name.toPascalCase()}")
            if (definition.hasSelectableSubscriptions()) {
                add("${definition.name.toPascalCase()}Subscription")
            }
        }
        metaDefinitions.forEach { definition ->
            add(definition.name.toPascalCase())
            add("Async${definition.name.toPascalCase()}")
        }
        add("SyncObjectFactories")
        add("AsyncObjectFactories")
    }

    return buildString {
        appendLine("from .objects import (")
        names.forEach { appendLine("    $it,") }
        appendLine(")")
        appendLine("from .types import MicrostepMode")
        appendLine()
        appendLine("__all__ = [")
        (names + "MicrostepMode").forEach { appendLine("    ${repr(it)},") }
        appendLine("]")
    }
}
