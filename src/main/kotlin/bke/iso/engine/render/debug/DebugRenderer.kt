package bke.iso.engine.render.debug

import bke.iso.engine.math.Box
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.Tile
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools

data class DebugSettings(
    var collisionBox: Boolean = true,
    var collisionBoxColor: Color = Color.GREEN,
    var collisionBoxSelected: Boolean = false,
    var position: Boolean = true,
    var positionColor: Color = Color.RED,
    var zAxis: Boolean = true,
    var zAxisColor: Color = Color.PURPLE
) : Component()

class DebugRenderer(batch: PolygonSpriteBatch) {

    private val shapeDrawer = DebugShapeDrawer(batch)
    private val shapes = Array<DebugShape>()
    private var enabled = false

    fun toggle() {
        enabled = enabled.not()
    }

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        if (enabled) {
            Pools.obtain(DebugLine::class.java).apply {
                this.start.set(start)
                this.end.set(end)
                this.end.set(end)
                this.width = width
                this.color = color
                shapes.add(this)
            }
        }
    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        if (enabled) {
            Pools.obtain(DebugRectangle::class.java).apply {
                this.rectangle.set(rectangle)
                this.lineWidth = lineWidth
                this.color = color
                shapes.add(this)
            }
        }
    }

    fun addCircle(pos: Vector3, radius: Float, color: Color) {
        if (enabled) {
            Pools.obtain(DebugCircle::class.java).apply {
                this.pos.set(pos.x, pos.y, pos.z)
                this.radius = radius
                this.color = color
                shapes.add(this)
            }
        }
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        if (enabled) {
            Pools.obtain(DebugPoint::class.java).apply {
                this.pos.set(pos)
                this.size = size
                this.color = color
                shapes.add(this)
            }
        }
    }

    fun addBox(box: Box, width: Float, color: Color) {
        if (enabled) {
            box.segments.forEach { segment ->
                addLine(segment.a, segment.b, width, color)
            }
        }
    }

    fun addSphere(pos: Vector3, radius: Float, color: Color) {
        if (enabled) {
            Pools.obtain(DebugSphere::class.java).apply {
                this.pos.set(pos.x, pos.y, pos.z)
                this.radius = radius
                this.color = color
                shapes.add(this)
            }
        }
    }

    fun add(actor: Actor) {
        if (!enabled) {
            return
        }
        val settings = actor.get<DebugSettings>() ?: return
        if (settings.collisionBox) {
            val color = if (settings.collisionBoxSelected) Color.PURPLE else settings.collisionBoxColor
            actor.getCollisionBox()?.let { box ->
                addBox(box, 1f, color)
            }
            settings.collisionBoxSelected = false
        }

        if (settings.position) {
            addPoint(actor.pos, 2f, settings.positionColor)
        }

        if (settings.zAxis && actor.z > 0f) {
            val start = Vector3(actor.x, actor.y, 0f)
            val end = actor.pos
            addPoint(start, 2f, settings.zAxisColor)
            addLine(start, end, 1f, settings.zAxisColor)
        }
    }

    fun add(tile: Tile) {
        if (!enabled) {
            return
        }
        val color = if (tile.selected) Color.PURPLE else Color.WHITE
        addBox(tile.getCollisionBox(), 1f, color)
        tile.selected = false
    }

    fun draw() {
        if (enabled) {
            shapeDrawer.begin()
            shapes.forEach(::drawShape)
            shapeDrawer.end()
        }
        Pools.freeAll(shapes)
        shapes.clear()
    }

    private fun drawShape(shape: DebugShape) =
        when (shape) {
            is DebugLine -> shapeDrawer.drawLine(shape)
            is DebugRectangle -> shapeDrawer.drawRectangle(shape)
            is DebugCircle -> shapeDrawer.drawCircle(shape)
            is DebugPoint -> shapeDrawer.drawPoint(shape)
            is DebugSphere -> shapeDrawer.drawSphere(shape)
        }
}
