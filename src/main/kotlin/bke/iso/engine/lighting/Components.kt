package bke.iso.engine.lighting

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO: add editor support
@Serializable
@SerialName("pointLight")
data class PointLight(
    val intensity: Float = 0f,
    val falloff: Float = 0.1f,
    val r: Float = 0f,
    val g: Float = 0f,
    val b: Float = 0f,
) : Component
