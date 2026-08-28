package bke.iso.engine.lighting

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("pointLight")
data class PointLight(
    var intensity: Float = 1f,
    var radius: Float = 4f,
    @Contextual
    var color: Color = Color.WHITE,
    @Contextual
    var offset: Vector3 = Vector3()
) : Component

@Serializable
@SerialName("fullBright")
class FullBright : Component
