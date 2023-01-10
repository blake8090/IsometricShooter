package bke.iso.engine.render

import bke.iso.engine.entity.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

data class Sprite(
    val texture: String,
    val offsetX: Float,
    val offsetY: Float
) : Component()

/**
 * Contains shapes drawn when debug mode is active.
 * This data is only for the current frame, and will be removed after rendering.
 */
data class DebugData(
    val lines: MutableList<DebugLine> = mutableListOf(),
    val rectangles: MutableList<DebugRectangle> = mutableListOf(),
    val circles: MutableList<DebugCircle> = mutableListOf(),
    val points: MutableList<DebugPoint> = mutableListOf()
) : Component()

// TODO: when implementing z-levels, change this to use a Segment
// TODO: implement width
data class DebugLine(
    val start: Vector2,
    val end: Vector2,
    val width: Float,
    val color: Color
) : Component()

data class DebugRectangle(
    val rectangle: Rectangle,
    // TODO: implement line width
    val lineWidth: Float,
    val color: Color
) : Component()

data class DebugCircle(
    val radius: Float,
    val color: Color
) : Component()

data class DebugPoint(
    val pos: Vector2,
    val size: Float,
    val color: Color
)
