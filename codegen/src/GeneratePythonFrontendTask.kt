package io.github.elektrofuzzis.ftswarm_clients.codegen

import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getIoDefinitions
import io.github.elektrofuzzis.ftswarm_clients.codegen.collector.getMetaDefinitions
import io.github.elektrofuzzis.ftswarm_clients.codegen.python.pythonGeneratedUnits
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path

@TaskAction
fun generatePythonSources(
    @Input apiDefPath: Path,
    @Output generatedSourceDir: Path,
) {
    generatedSourceDir.toFile().let {
        it.deleteRecursively()
        it.mkdirs()
    }

    pythonGeneratedUnits(
        ioDefinitions = getIoDefinitions(apiDefPath),
        metaDefinitions = getMetaDefinitions(apiDefPath),
    ).forEach { it.write(apiDefPath, generatedSourceDir) }
}
