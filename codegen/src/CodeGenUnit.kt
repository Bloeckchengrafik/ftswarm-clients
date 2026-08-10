package io.github.elektrofuzzis.ftswarm_clients.codegen

import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeText

abstract class CodeGenUnit(private val filename: String) {
    abstract fun getCode(apiDefPath: Path): String

    fun write(
        apiDefPath: Path,
        generatedSourceDir: Path,
    ) {
        val file = generatedSourceDir / filename
        val code = getCode(apiDefPath)
        file.writeText(code)
    }

    companion object {
        operator fun invoke(filename: String, block: (Path) -> String) = object : CodeGenUnit(filename) {
            override fun getCode(apiDefPath: Path): String = block(apiDefPath)
        }
    }
}