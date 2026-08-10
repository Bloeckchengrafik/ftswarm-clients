package io.github.elektrofuzzis.ftswarm_clients.codegen

import io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin.kotlinIoTypeUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin.kotlinIoObjectUnits
import io.github.elektrofuzzis.ftswarm_clients.codegen.kotlin.kotlinVendorIdUnit
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getIoDefinitions
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path

@TaskAction
fun generateKotlinSources(
    @Input apiDefPath: Path,
    @Output generatedSourceDir: Path,
) {
    generatedSourceDir.toFile().let {
        it.deleteRecursively()
        it.mkdirs()
    }
    val units = mutableListOf<CodeGenUnit>()
    units += kotlinVendorIdUnit()
    units += kotlinIoTypeUnit()
    units += kotlinIoObjectUnits(getIoDefinitions(apiDefPath))

    units.forEach { it.write(apiDefPath, generatedSourceDir) }
}
