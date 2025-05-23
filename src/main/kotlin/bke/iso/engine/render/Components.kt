package bke.iso.engine.render

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("sprite")
data class Sprite(
    var texture: String = "",
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var alpha: Float = 1f,
    var scale: Float = 1f,
    var rotation: Float = 0f
) : Component

@Serializable
@SerialName("spriteFillColor")
data class SpriteFillColor(
    var r: Float = 0f,
    var g: Float = 0f,
    var b: Float = 0f
) : Component

@Serializable
@SerialName("spriteTintColor")
data class SpriteTintColor(
    var r: Float = 0f,
    var g: Float = 0f,
    var b: Float = 0f
) : Component

// TODO: don't bother serializing this, add it dynamically
@Serializable
@SerialName("debugSettings")
data class DebugSettings(
    var collisionBox: Boolean = true,
    @Contextual
    var collisionBoxColor: Color = Color.GREEN,
    var collisionBoxSelected: Boolean = false,
    var position: Boolean = true,
    @Contextual
    var positionColor: Color = Color.RED,
    var zAxis: Boolean = true,
    @Contextual
    var zAxisColor: Color = Color.PURPLE
) : Component

@Serializable
@SerialName("occlude")
class Occlude : Component
