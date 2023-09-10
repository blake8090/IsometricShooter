package bke.iso.engine

import bke.iso.engine.world.Component
import bke.iso.engine.world.ComponentSubType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import mu.KotlinLogging
import org.reflections.Reflections

class Serializer {

    private val log = KotlinLogging.logger {}
    private val mapper = ObjectMapper()

    fun start() {
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
}
