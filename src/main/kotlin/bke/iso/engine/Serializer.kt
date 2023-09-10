package bke.iso.engine

import bke.iso.engine.world.Component
import bke.iso.engine.world.ComponentSubType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.reflections.Reflections

class Serializer {

    private val log = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    fun start() {
        mapper.enable(
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
            JsonParser.Feature.ALLOW_COMMENTS
        )
        mapper.enable(SerializationFeature.INDENT_OUTPUT)

        registerComponentSubTypes()
    }

    private fun registerComponentSubTypes() {
        val subTypes = Reflections("bke.iso")
            .getSubTypesOf(Component::class.java)
        for (subType in subTypes) {
            val annotation = requireNotNull(subType.getAnnotation(ComponentSubType::class.java)) {
                "Component '${subType.name}' must have a ComponentSubType annotation"
            }
            val name = annotation.name
            mapper.registerSubtypes(NamedType(subType, name))
            log.debug { "Registered Component sub type: '$name' to '${subType.name}'" }
        }
    }

    fun <T : Any> write(value: T): String =
        mapper.writeValueAsString(value)
}
