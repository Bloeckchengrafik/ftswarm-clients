package io.github.elektrofuzzis.ftswarm_clients.codegen.collector

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

@Serializable
data class IoDefinition(
    val name: String,
    val init: List<InitStep> = emptyList(),
    val subscription: SubscriptionDefinition? = null,
    val members: Map<String, FunctionDefinition> = emptyMap(),
)

@Serializable
sealed interface InitStep {
    @Serializable
    @SerialName("set_io_type")
    data class SetIoType(
        val type: String,
        val parameters: List<ParameterDefinition> = emptyList(),
    ) : InitStep

    @Serializable
    @SerialName("subscribe")
    data object Subscribe : InitStep
}

@Serializable
sealed interface SubscriptionDefinition {
    @Serializable
    @SerialName("value")
    data class Value(
        val parser: ApiType,
        val initialValue: FunctionDefinition,
    ) : SubscriptionDefinition

    @JvmInline
    @Serializable
    @SerialName("bitset")
    value class Bitset(
        val entries: List<BitsetEntry>,
    ) : SubscriptionDefinition
}

@Serializable
data class BitsetEntry(
    val name: String,
    val enabled: SubscriptionEnablement,
    val mask: Int,
    val parser: ApiType,
    val initialValue: FunctionDefinition,
)

@Serializable
enum class SubscriptionEnablement {
    @SerialName("always")
    Always,

    @SerialName("default")
    Default,

    @SerialName("requested")
    Requested,
}

@Serializable
data class FunctionDefinition(
    val command: String,
    val parameters: List<ParameterDefinition> = emptyList(),
    val returnType: ApiType,
)

@Serializable
data class ParameterDefinition(
    val name: String,
    val type: ApiType,
    val defaultValue: YamlNode? = null,
) {
    fun defaultValueText(): String? = (defaultValue as? YamlScalar)?.content
}

@Serializable
enum class ApiType {
    @SerialName("boolean")
    Boolean,

    @SerialName("int")
    Int,

    @SerialName("float")
    Float,

    @SerialName("string")
    String,

    @SerialName("ok")
    Ok,
}

fun getIoDefinitions(basePath: Path): List<IoDefinition> {
    val templates = loadTemplates(basePath / "templates")
    val definitionFiles = yamlFiles(basePath / "inputs") + yamlFiles(basePath / "outputs")

    return definitionFiles
        .map { path -> decodeYamlNode<IoDefinition>(resolveTemplate(path, templates)) }
        .also(::validateIoDefinitions)
}

private fun loadTemplates(directory: Path): Map<String, YamlMap> =
    yamlFiles(directory).associate { path ->
        val root = readRootMap(path, allowEmpty = true)
        require(root.getKey("extends") == null) { "$path: templates cannot extend other templates" }
        path.nameWithoutExtension to root
    }

private fun resolveTemplate(path: Path, templates: Map<String, YamlMap>): YamlMap {
    val concrete = readRootMap(path)
    val extendsEntry = concrete.entries.entries.singleOrNull { it.key.content == "extends" }
    val templateName = when (val node = extendsEntry?.value) {
        null -> null
        is YamlScalar -> node.content
        else -> throw IllegalArgumentException("$path: extends must be a template name")
    }
    val withoutExtends = concrete.copy(
        entries = concrete.entries.filterKeys { it.content != "extends" },
    )

    if (templateName == null) return withoutExtends
    val template = templates[templateName]
        ?: throw IllegalArgumentException("$path: unknown template '$templateName'")
    return mergeYamlMaps(template, withoutExtends)
}

private fun readRootMap(path: Path, allowEmpty: Boolean = false): YamlMap =
    when (val root = readYamlNode(path)) {
        is YamlMap -> root
        is YamlNull -> {
            require(allowEmpty) { "$path: expected a YAML mapping" }
            YamlMap(emptyMap(), root.path)
        }
        else -> throw IllegalArgumentException("$path: expected a YAML mapping")
    }

private fun yamlFiles(directory: Path): List<Path> {
    if (!directory.exists()) return emptyList()
    require(directory.isDirectory()) { "$directory: expected a directory" }
    return directory.listDirectoryEntries("*.yaml").sortedBy { it.fileName.toString() }
}

private fun mergeYaml(base: YamlNode, concrete: YamlNode): YamlNode =
    when {
        base is YamlMap && concrete is YamlMap -> mergeYamlMaps(base, concrete)
        base is YamlTaggedNode && concrete is YamlTaggedNode && base.tag == concrete.tag ->
            concrete.copy(innerNode = mergeYaml(base.innerNode, concrete.innerNode))
        else -> concrete
    }

private fun mergeYamlMaps(base: YamlMap, concrete: YamlMap): YamlMap {
    val entries = base.entries.toMutableMap()

    concrete.entries.forEach { (concreteKey, concreteValue) ->
        val inheritedKey = entries.keys.singleOrNull { it.content == concreteKey.content }
        val inheritedValue = inheritedKey?.let(entries::get)
        if (inheritedKey != null) entries.remove(inheritedKey)

        if (concreteValue !is YamlNull) {
            entries[concreteKey] = inheritedValue?.let { mergeYaml(it, concreteValue) } ?: concreteValue
        }
    }

    return YamlMap(entries, concrete.path)
}

private fun validateIoDefinitions(definitions: List<IoDefinition>) {
    val duplicateNames = definitions.groupBy { it.name }.filterValues { it.size > 1 }.keys
    require(duplicateNames.isEmpty()) { "Duplicate IO definitions: ${duplicateNames.joinToString()}" }

    definitions.forEach(::validateIoDefinition)
}

private fun validateIoDefinition(definition: IoDefinition) {
    val subscribes = definition.init.count { it is InitStep.Subscribe }
    require((definition.subscription != null) == (subscribes == 1)) {
        "${definition.name}: subscription definitions require exactly one subscribe init step"
    }

    definition.members.forEach { (name, function) ->
        require(name.isNotBlank()) { "${definition.name}: member names cannot be blank" }
        validateFunction(definition.name, function)
    }

    definition.init.filterIsInstance<InitStep.SetIoType>().forEach { step ->
        step.parameters.forEach { validateParameter(definition.name, it) }
    }

    when (val subscription = definition.subscription) {
        is SubscriptionDefinition.Value -> {
            validateFunction(definition.name, subscription.initialValue)
            require(subscription.parser != ApiType.Ok) {
                "${definition.name}: ok cannot be used as a subscription parser"
            }
            require(subscription.parser == subscription.initialValue.returnType) {
                "${definition.name}: subscription parser and initial value must have the same type"
            }
        }

        is SubscriptionDefinition.Bitset -> validateBitset(definition.name, subscription.entries)
        null -> Unit
    }
}

private fun validateBitset(ioName: String, entries: List<BitsetEntry>) {
    require(entries.isNotEmpty()) { "$ioName: bitset subscriptions require at least one entry" }
    require(entries.map { it.name }.distinct().size == entries.size) {
        "$ioName: bitset subscription names must be unique"
    }
    require(entries.map { it.mask }.distinct().size == entries.size) {
        "$ioName: bitset subscription masks must be unique"
    }

    entries.forEach { entry ->
        require(entry.mask > 0 && entry.mask and (entry.mask - 1) == 0) {
            "$ioName.${entry.name}: subscription mask must contain exactly one bit"
        }
        validateFunction(ioName, entry.initialValue)
        require(entry.parser != ApiType.Ok) {
            "$ioName.${entry.name}: ok cannot be used as a subscription parser"
        }
        require(entry.parser == entry.initialValue.returnType) {
            "$ioName.${entry.name}: parser and initial value must have the same type"
        }
    }
}

private fun validateFunction(ioName: String, function: FunctionDefinition) {
    require(function.command.isNotBlank()) { "$ioName: command names cannot be blank" }
    function.parameters.forEach { validateParameter(ioName, it) }
}

private fun validateParameter(ioName: String, parameter: ParameterDefinition) {
    require(parameter.name.isNotBlank()) { "$ioName: parameter names cannot be blank" }
    val default = parameter.defaultValueText() ?: return
    val valid = when (parameter.type) {
        ApiType.Boolean -> default.toBooleanStrictOrNull() != null
        ApiType.Int -> default.toIntOrNull() != null
        ApiType.Float -> default.toFloatOrNull() != null
        ApiType.String -> true
        ApiType.Ok -> false
    }
    require(valid) { "$ioName.${parameter.name}: invalid ${parameter.type} default '$default'" }
}
