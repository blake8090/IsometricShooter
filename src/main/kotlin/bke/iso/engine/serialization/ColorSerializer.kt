package bke.iso.engine.serialization

import com.badlogic.gdx.graphics.Color
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

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Color") {
        element<Float>("r")
        element<Float>("g")
        element<Float>("b")
        element<Float>("a")
    }

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.r)
            encodeFloatElement(descriptor, 1, value.g)
            encodeFloatElement(descriptor, 2, value.b)
            encodeFloatElement(descriptor, 3, value.a)
        }
    }

    override fun deserialize(decoder: Decoder): Color =
        decoder.decodeStructure(descriptor) {
            // TODO: should this default to 0 to allow omitting fields?
            var r: Float? = null
            var g: Float? = null
            var b: Float? = null
            var a: Float? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> r = decodeFloatElement(descriptor, 0)
                    1 -> g = decodeFloatElement(descriptor, 1)
                    2 -> b = decodeFloatElement(descriptor, 2)
                    3 -> a = decodeFloatElement(descriptor, 3)
                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            Color(
                requireNotNull(r),
                requireNotNull(g),
                requireNotNull(b),
                requireNotNull(a)
            )
        }
}
