package bke.iso.engine.render

import bke.iso.engine.world.Component
import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("sprite")
data class Sprite(
    val texture: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    var alpha: Float = 1f,
    var scale: Float = 1f
) : Component()

@JsonTypeName("debugSettings")
data class DebugSettings(
    var collisionBox: Boolean = true,
    var collisionBoxColor: Color = Color.GREEN,
    var collisionBoxSelected: Boolean = false,
    var position: Boolean = true,
    var positionColor: Color = Color.RED,
    var zAxis: Boolean = true,
    var zAxisColor: Color = Color.PURPLE
) : Component()
