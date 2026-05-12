package bke.iso.engine.serialization

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
            // TODO: should this default to 0 to allow omitting fields?
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
