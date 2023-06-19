package bke.iso.engine.render.debug

import bke.iso.engine.physics.collision.Box
import bke.iso.service.SingletonService
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool

class DebugRenderService : SingletonService {

    private val lines = mutableListOf<DebugLine>()
    private val linePool = object : Pool<DebugLine>() {
        override fun newObject() =
            DebugLine()
    }

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

    private val boxes = mutableListOf<DebugBox>()
    private val boxPool = object : Pool<DebugBox>() {
        override fun newObject() =
            DebugBox()
    }

    private val spheres = mutableListOf<DebugSphere>()
    private val spherePool = object : Pool<DebugSphere>() {
        override fun newObject() =
            DebugSphere()
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

    fun addBox(pos: Vector3, dimensions: Vector3, color: Color) {
        val box = boxPool.obtain()
        box.pos.set(pos)
        box.dimensions.set(dimensions)
        box.color = color
        boxes.add(box)
    }

    fun addBox(box: Box, color: Color) {
        for (segment in box.getSegments()) {
            addLine(segment.a, segment.b, 1f, color)
        }
    }

    fun addSphere(pos: Vector3, radius: Float, color: Color) {
        val sphere = spherePool.obtain()
        sphere.pos.set(pos.x, pos.y, pos.z)
        sphere.radius = radius
        sphere.color = color
        spheres.add(sphere)
    }

    fun render(shapeDrawer: DebugShapeDrawer) {
        shapeDrawer.begin()
        lines.forEach(shapeDrawer::drawLine)
        rectangles.forEach(shapeDrawer::drawRectangle)
        circles.forEach(shapeDrawer::drawCircle)
        points.forEach(shapeDrawer::drawPoint)
        boxes.forEach(shapeDrawer::drawBox)
        spheres.forEach(shapeDrawer::drawSphere)
        shapeDrawer.end()
    }

    fun clear() {
        lines.forEach(linePool::free)
        lines.clear()

        rectangles.forEach(rectanglePool::free)
        rectangles.clear()

        circles.forEach(circlePool::free)
        circles.clear()

        points.forEach(pointPool::free)
        points.clear()

        boxes.forEach(boxPool::free)
        boxes.clear()

        spheres.forEach(spherePool::free)
        spheres.clear()
    }
}
