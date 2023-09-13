package bke.iso.engine

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class Serializer {

    private val module = SerializersModule {
        contextual(Vector3Serializer)
        polymorphic(Component::class) {
            for (kClass in getComponentSubTypes()) {
                subclass(kClass)
            }
        }
    }

    val format: Json = Json {
        serializersModule = module
        isLenient = true
    }

    private fun getComponentSubTypes(): List<KClass<out Component>> =
        Reflections("bke.iso")
            .getSubTypesOf(Component::class.java)
            .map(Class<out Component>::kotlin)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Component> PolymorphicModuleBuilder<Component>.subclass(kClass: KClass<T>) {
        subclass(kClass, serializer(kClass.createType()) as KSerializer<T>)
    }

    inline fun <reified T : Any> read(content: String): T =
        format.decodeFromString(content)
}

object Vector3Serializer : KSerializer<Vector3> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vector3") {
        element<Float>("x")
        element<Float>("y")
        element<Float>("z")
    }

    override fun serialize(encoder: Encoder, value: Vector3) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
            encodeFloatElement(descriptor, 2, value.z)
        }
    }

    override fun deserialize(decoder: Decoder): Vector3 =
        decoder.decodeStructure(descriptor) {
            var x: Float? = null
            var y: Float? = null
            var z: Float? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> x = decodeFloatElement(descriptor, 0)
                    1 -> y = decodeFloatElement(descriptor, 1)
                    2 -> z = decodeFloatElement(descriptor, 2)
                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            Vector3(
                requireNotNull(x),
                requireNotNull(y),
                requireNotNull(z)
            )
        }
}