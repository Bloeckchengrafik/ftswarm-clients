package io.github.elektrofuzzis.ftswarm_clients.codegen.collector

import java.nio.file.Path
import kotlin.io.path.div

data class IoType(
    val id: Int,
    val constantName: String,
)

fun getIoTypes(basePath: Path) = readYaml<List<String>>(basePath / "iotypes.yaml")
    .mapIndexed(::IoType)
