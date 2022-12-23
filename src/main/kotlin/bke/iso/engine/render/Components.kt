package bke.iso.engine.render

import bke.iso.engine.entity.Component
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

data class Sprite(
    val texture: String,
    val offsetX: Float,
    val offsetY: Float
) : Component()

data class DebugLine(
    val start: Vector2,
    val end: Vector2
) : Component()

data class DebugRectangle(
    val rectangle: Rectangle
) : Component()
