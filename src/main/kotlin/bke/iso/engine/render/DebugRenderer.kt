package bke.iso.engine.render

import bke.iso.engine.math.Box2
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools

class DebugRenderer(private val shapeDrawer: DebugShapeDrawer) {

    private val lines = Array<DebugLine>()
    private val rectangles = Array<DebugRectangle>()
    private val circles = Array<DebugCircle>()
    private val points = Array<DebugPoint>()
    private val spheres = Array<DebugSphere>()

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        Pools.obtain(DebugLine::class.java).apply {
            this.start.set(start)
            this.end.set(end)
            this.end.set(end)
            this.width = width
            this.color = color
            lines.add(this)
        }
    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        Pools.obtain(DebugRectangle::class.java).apply {
            this.rectangle.set(rectangle)
            this.lineWidth = lineWidth
            this.color = color
            rectangles.add(this)
        }
    }

    fun addCircle(pos: Vector3, radius: Float, color: Color) {
        Pools.obtain(DebugCircle::class.java).apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
            circles.add(this)
        }
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        Pools.obtain(DebugPoint::class.java).apply {
            this.pos.set(pos)
            this.size = size
            this.color = color
            points.add(this)
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
            spheres.add(this)
        }
    }

    fun render() {
        shapeDrawer.begin()
        lines.forEach(shapeDrawer::drawLine)
        rectangles.forEach(shapeDrawer::drawRectangle)
        circles.forEach(shapeDrawer::drawCircle)
        points.forEach(shapeDrawer::drawPoint)
        spheres.forEach(shapeDrawer::drawSphere)
        shapeDrawer.end()
    }

    fun clear() {
        clear(lines)
        clear(rectangles)
        clear(circles)
        clear(points)
        clear(spheres)
    }

    private fun <T : Any> clear(array: Array<T>) {
        Pools.freeAll(array)
        array.clear()
    }
}
