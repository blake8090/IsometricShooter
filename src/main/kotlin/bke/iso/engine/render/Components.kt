package bke.iso.engine.render

import bke.iso.engine.world.Component
import bke.iso.engine.world.ComponentSubType
import com.badlogic.gdx.graphics.Color

@ComponentSubType("sprite")
data class Sprite(
    val texture: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    var alpha: Float = 1f,
    var scale: Float = 1f
) : Component()

@ComponentSubType("debugSettings")
data class DebugSettings(
    var collisionBox: Boolean = true,
    var collisionBoxColor: Color = Color.GREEN,
    var collisionBoxSelected: Boolean = false,
    var position: Boolean = true,
    var positionColor: Color = Color.RED,
    var zAxis: Boolean = true,
    var zAxisColor: Color = Color.PURPLE
) : Component()
