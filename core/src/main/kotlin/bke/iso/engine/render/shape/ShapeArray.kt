package bke.iso.engine.render.shape

import bke.iso.engine.math.Box
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.PoolManager

object ShapePools {
    val manager = PoolManager().apply {
        addPool { Line3D() }
        addPool { Rectangle2D() }
        addPool { Circle3D() }
        addPool { Point3D() }
        addPool { Sphere3D() }
    }
}

/**
 * Provides an interface for creating and storing pooled [Shape]s.
 */
class ShapeArray {

    private val shapes = Array<Shape>()

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        ShapePools.manager.obtain(Line3D::class.java).apply {
            this.start.set(start)
            this.end.set(end)
            this.end.set(end)
            this.width = width
            this.color = color
            shapes.add(this)
        }
    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        ShapePools.manager.obtain(Rectangle2D::class.java).apply {
            this.pos.set(rectangle.x, rectangle.y)
            this.size.set(rectangle.width, rectangle.height)
            this.lineWidth = lineWidth
            this.color = color
            shapes.add(this)
        }
    }

    fun addCircle(pos: Vector3, radius: Float, color: Color) {
        ShapePools.manager.obtain(Circle3D::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            shapes.add(this)
        }
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        ShapePools.manager.obtain(Point3D::class.java).apply {
            this.pos.set(pos)
            this.size = size
            this.color = color
            shapes.add(this)
        }
    }

    fun addBox(box: Box, width: Float, color: Color) {
        for (segment in box.getSegments()) {
            addLine(segment.a, segment.b, width, color)
        }
    }

    fun addSphere(pos: Vector3, radius: Float, color: Color) {
        ShapePools.manager.obtain(Sphere3D::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            shapes.add(this)
        }
    }

    operator fun iterator(): Array.ArrayIterator<Shape> =
        shapes.iterator()

    fun clear() {
        for (shape in shapes) {
            ShapePools.manager.free(shape)
        }
        shapes.clear()
    }
}
