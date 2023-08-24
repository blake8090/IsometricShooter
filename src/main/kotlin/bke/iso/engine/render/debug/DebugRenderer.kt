package bke.iso.engine.render.debug

import bke.iso.engine.math.Box2
import bke.iso.engine.world.Component
import com.badlogic.gdx.graphics.Color
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

class DebugRenderer(private val shapeDrawer: DebugShapeDrawer) {

    private val shapes = Array<DebugShape>()

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        Pools.obtain(DebugLine::class.java).apply {
            this.start.set(start)
            this.end.set(end)
            this.end.set(end)
            this.width = width
            this.color = color
            shapes.add(this)
        }
    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        Pools.obtain(DebugRectangle::class.java).apply {
            this.rectangle.set(rectangle)
            this.lineWidth = lineWidth
            this.color = color
            shapes.add(this)
        }
    }

    fun addCircle(pos: Vector3, radius: Float, color: Color) {
        Pools.obtain(DebugCircle::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            shapes.add(this)
        }
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        Pools.obtain(DebugPoint::class.java).apply {
            this.pos.set(pos)
            this.size = size
            this.color = color
            shapes.add(this)
        }
    }

    fun addBox(box: Box2, width: Float, color: Color) {
        box.segments.forEach { segment ->
            addLine(segment.a, segment.b, width, color)
        }
    }

    fun addSphere(pos: Vector3, radius: Float, color: Color) {
        Pools.obtain(DebugSphere::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            shapes.add(this)
        }
    }

    fun render() {
        shapeDrawer.begin()
        for (shape in shapes) {
            when (shape) {
                is DebugLine -> shapeDrawer.drawLine(shape)
                is DebugRectangle -> shapeDrawer.drawRectangle(shape)
                is DebugCircle -> shapeDrawer.drawCircle(shape)
                is DebugPoint -> shapeDrawer.drawPoint(shape)
                is DebugSphere -> shapeDrawer.drawSphere(shape)
            }
        }
        shapeDrawer.end()
    }

    fun clear() {
        Pools.freeAll(shapes)
        shapes.clear()
    }
}
