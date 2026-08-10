package io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin

import io.github.elektrofuzzis.ftswarm_clients.codegen.CodeGenUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getIoTypes

fun kotlinIoTypeUnit() = CodeGenUnit("IOTypes.kt") { apiDefPath ->
    // language="kotlin"
    return@CodeGenUnit """
|package github.elektrofuzzis.ftswarm_clients.kotlin.codegen
|
|object IOTypes {
|${
    getIoTypes(apiDefPath).joinToString("\n") {
        "    const val ${it.constantName} = ${it.id}"
    }
}
|}
    """.trimMargin()
}