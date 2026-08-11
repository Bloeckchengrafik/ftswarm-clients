package io.github.elektrofuzzis.ftswarm_clients.codegen.collector

import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.div

@Serializable
data class MicrostepModeDefinition(
    val name: String,
    val value: Int,
)

fun getMicrostepModes(basePath: Path): List<MicrostepModeDefinition> =
    readYaml<List<MicrostepModeDefinition>>(basePath / "microstep-modes.yaml").also { modes ->
        require(modes.isNotEmpty()) { "At least one microstep mode must be defined" }
        require(modes.map { it.name }.distinct().size == modes.size) {
            "Microstep mode names must be unique"
        }
        require(modes.map { it.value }.distinct().size == modes.size) {
            "Microstep mode values must be unique"
        }
    }
