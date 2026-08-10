package io.github.elektrofuzzis.ftswarm_clients.codegen.collector

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.decodeFromStream
import java.nio.file.Path

@PublishedApi
internal val yaml = Yaml(
    configuration = YamlConfiguration(
        anchorsAndAliases = AnchorsAndAliases.Permitted(),
    )
)

inline fun <reified T> readYaml(path: Path): T {
    return yaml.decodeFromStream<T>(path.toFile().inputStream())
}

fun readYamlNode(path: Path): YamlNode = yaml.parseToYamlNode(path.toFile().readText())

inline fun <reified T> decodeYamlNode(node: YamlNode): T = yaml.decodeFromYamlNode(node)
