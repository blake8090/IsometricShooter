package bke.iso.engine.lighting

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("pointLight")
data class PointLight(
    var intensity: Float = 0f,
    var falloff: Float = 0.1f,
    @Contextual
    var color: Color
) : Component

@Serializable
@SerialName("fullBright")
class FullBright : Component
