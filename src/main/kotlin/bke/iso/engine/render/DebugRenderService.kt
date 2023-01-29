package bke.iso.engine.render

import bke.iso.service.Singleton
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool

@Singleton
class DebugRenderService {

    private val lines = mutableListOf<DebugLine>()
    private val linePool = object : Pool<DebugLine>() {
        override fun newObject() =
            DebugLine()
    }

    // TODO: currently not used, should we remove this?
    private val rectangles = mutableListOf<DebugRectangle>()
    private val rectanglePool = object : Pool<DebugRectangle>() {
        override fun newObject() =
            DebugRectangle()
    }

    private val circles = mutableListOf<DebugCircle>()
    private val circlePool = object : Pool<DebugCircle>() {
        override fun newObject() =
            DebugCircle()
    }

    private val points = mutableListOf<DebugPoint>()
    private val pointPool = object : Pool<DebugPoint>() {
        override fun newObject() =
            DebugPoint()
    }

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        val line = linePool.obtain()
        line.start.set(start)
        line.end.set(end)
        line.width = width
        line.color = color
        lines.add(line)
    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        val rect = rectanglePool.obtain()
        rect.rectangle.set(rectangle)
        rect.lineWidth = lineWidth
        rect.color = color
        rectangles.add(rect)
    }

    fun addCircle(pos: Vector3, radius: Float, color: Color) {
        val circle = circlePool.obtain()
        circle.pos.set(pos.x, pos.y, pos.z)
        circle.radius = radius
        circle.color = color
        circles.add(circle)
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        val point = pointPool.obtain()
        point.pos.set(pos)
        point.size = size
        point.color = color
        points.add(point)
    }

    fun render(shapeRenderHelper: ShapeRenderHelper) {
        for (line in lines) {
            shapeRenderHelper.drawLine(line.start, line.end, line.color)
        }

        for (circle in circles) {
            shapeRenderHelper.drawCircle(circle.pos, circle.radius, circle.color)
        }

        for (point in points) {
            shapeRenderHelper.drawPoint(point.pos, point.size, point.color)
        }

        reset()
    }

    private fun reset() {
        lines.forEach(linePool::free)
        lines.clear()

        rectangles.forEach(rectanglePool::free)
        rectangles.clear()

        circles.forEach(circlePool::free)
        circles.clear()

        points.forEach(pointPool::free)
        points.clear()
    }
}
