package bke.iso.engine

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.reflections.Reflections

class Serializer {

    private val mapper = jacksonObjectMapper()

    fun start() {
        mapper.apply {
            enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            enable(JsonParser.Feature.ALLOW_COMMENTS)
            enable(SerializationFeature.INDENT_OUTPUT)
            configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(), false)
            addMixIn(Vector3::class.java, Vector3Mixin::class.java)
        }

        val componentSubTypes = Reflections("bke.iso")
            .getSubTypesOf(Component::class.java)
            .toTypedArray()
        mapper.registerSubtypes(*componentSubTypes)
    }

    fun <T : Any> write(value: T): String =
        mapper.writeValueAsString(value)
}

@Suppress("UNUSED")
private abstract class Vector3Mixin {
    @JsonIgnore
    abstract fun isZero(): Boolean
    @JsonIgnore
    abstract fun isUnit(): Boolean
}
