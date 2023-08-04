package bke.iso.v2.engine.render

import bke.iso.engine.math.Box
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3

class DebugRenderer {

    private val lines = ObjectPool.new<DebugLine>()
    private val rectangles = ObjectPool.new<DebugRectangle>()
    private val circles = ObjectPool.new<DebugCircle>()
    private val points = ObjectPool.new<DebugPoint>()
    private val boxes = ObjectPool.new<DebugBox>()
    private val spheres = ObjectPool.new<DebugSphere>()

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) =
        lines.obtain().apply {
            this.start.set(start)
            this.end.set(end)
            this.end.set(end)
            this.width = width
            this.color = color
        }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) =
        rectangles.obtain().apply {
            this.rectangle.set(rectangle)
            this.lineWidth = lineWidth
            this.color = color
        }

    fun addCircle(pos: Vector3, radius: Float, color: Color) =
        circles.obtain().apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
        }

    fun addPoint(pos: Vector3, size: Float, color: Color) =
        points.obtain().apply {
            this.pos.set(pos)
            this.size = size
            this.color = color
        }

    fun addBox(box: Box, color: Color) =
        boxes.obtain().apply {
            pos.set(box.min)
            dimensions.set(box.width, box.length, box.height)
            this.color = color
        }

    fun addSphere(pos: Vector3, radius: Float, color: Color) =
        spheres.obtain().apply {
            this.pos.set(pos.x, pos.y, pos.z)
            this.radius = radius
            this.color = color
        }

    fun render(shapeDrawer: DebugShapeDrawer) {
        shapeDrawer.begin()
        lines.getAll().forEach(shapeDrawer::drawLine)
        rectangles.getAll().forEach(shapeDrawer::drawRectangle)
        circles.getAll().forEach(shapeDrawer::drawCircle)
        points.getAll().forEach(shapeDrawer::drawPoint)
        boxes.getAll().forEach(shapeDrawer::drawBox)
        spheres.getAll().forEach(shapeDrawer::drawSphere)
        shapeDrawer.end()
    }

    fun clear() {
        lines.clear()
        rectangles.clear()
        circles.clear()
        points.clear()
        boxes.clear()
        spheres.clear()
    }
}
