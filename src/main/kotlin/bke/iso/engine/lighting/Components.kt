package bke.iso.engine.lighting

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("pointLight")
data class PointLight(
    var intensity: Float = 0f,
    var falloff: Float = 0.1f,
    var r: Float = 0f,
    var g: Float = 0f,
    var b: Float = 0f,
) : Component

@Serializable
@SerialName("fullBright")
class FullBright : Component
