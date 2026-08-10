package io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin

import io.github.elektrofuzzis.ftswarm_clients.codegen.CodeGenUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getVendorIds

fun kotlinVendorIdUnit() = CodeGenUnit("VendorIdSet.kt") { apiDefPath ->
    val ids = getVendorIds(apiDefPath)

    // language="kotlin"
    return@CodeGenUnit """
            |package github.elektrofuzzis.ftswarm_clients.kotlin.codegen
            |
            |data class VendorIdFilter(val vid: Int, val pid: Int)
            |
            |val vendorIds = listOf(${
        ids.joinToString(", ") {
            "VendorIdFilter(${it.vid}, ${it.pid})"
        }
    })""".trimMargin()
}