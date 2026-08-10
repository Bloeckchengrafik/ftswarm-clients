package io.github.elektrofuzzis.ftswarm_clients.codegen.collector

import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.div

@Serializable
data class VendorIdSet(
    val vid: Int,
    val pid: Int,
)

fun getVendorIds(basePath: Path): List<VendorIdSet> = readYaml(basePath / "vendor-ids.yaml")
