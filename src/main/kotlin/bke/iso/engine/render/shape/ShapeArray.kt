package bke.iso.engine.render.shape

import bke.iso.engine.math.Box
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools

/**
 * Provides an interface for creating and storing pooled [Shape]s.
 */
class ShapeArray {

    private val shapes = Array<Shape>()

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        Pools.obtain(Line3D::class.java).apply {
            this.start.set(start)
            this.end.set(end)
            this.end.set(end)
            this.width = width
            this.color = color
            shapes.add(this)
        }
    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        Pools.obtain(Rectangle2D::class.java).apply {
            this.pos.set(rectangle.x, rectangle.y)
            this.size.set(rectangle.width, rectangle.height)
            this.lineWidth = lineWidth
            this.color = color
            shapes.add(this)
        }
    }

    fun addCircle(pos: Vector3, radius: Float, color: Color) {
        Pools.obtain(Circle3D::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            shapes.add(this)
        }
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        Pools.obtain(Point3D::class.java).apply {
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
        Pools.obtain(Sphere3D::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            shapes.add(this)
        }
    }

    operator fun iterator(): Array.ArrayIterator<Shape> =
        shapes.iterator()

    fun clear() {
        Pools.freeAll(shapes)
        shapes.clear()
    }
}
