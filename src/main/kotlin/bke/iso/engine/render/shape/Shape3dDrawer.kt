package bke.iso.engine.render.shape

import bke.iso.engine.math.TILE_SIZE_X
import bke.iso.engine.math.TILE_SIZE_Y
import bke.iso.engine.math.TILE_SIZE_Z
import bke.iso.engine.math.getIsometricRatio
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.makePixelTexture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import space.earlygrey.shapedrawer.ShapeDrawer

class Shape3dDrawer(batch: PolygonSpriteBatch) {

    private val shapeDrawer: ShapeDrawer

    init {
        val pixel = makePixelTexture()
        val region = TextureRegion(pixel, 0, 0, 1, 1)
        shapeDrawer = ShapeDrawer(batch, region)
    }

    fun update() {
        shapeDrawer.update()
    }

    fun begin() {
        shapeDrawer.batch.begin()
    }

    fun end() {
        shapeDrawer.batch.end()
    }

    fun drawShape(shape: Shape3D) =
        when (shape) {
            is Line3D -> drawLine(shape)
//            is DebugRectangle -> shapeDrawer.drawRectangle(shape)
            is Circle3D -> drawCircle(shape)
            is Point3D -> drawPoint(shape)
            is Sphere3D -> drawSphere(shape)
        }

    fun drawPoint(point: Point3D) {
        drawPoint(point.pos, point.size, point.color)
    }

    private fun drawPoint(worldPos: Vector3, size: Float, color: Color) {
        shapeDrawer.filledCircle(toScreen(worldPos), size, color)
    }

//    fun drawRectangle(rectangle: DebugRectangle) {
//        val polygon = toScreen(rectangle.rectangle)
//        shapeDrawer.setColor(rectangle.color)
//        shapeDrawer.polygon(polygon, rectangle.lineWidth)
//    }

    fun drawLine(line: Line3D) {
        drawLine(line.start, line.end, line.color, line.width)
    }

    private fun drawLine(start: Vector3, end: Vector3, color: Color, width: Float = 1f) {
        shapeDrawer.setColor(color)
        shapeDrawer.line(toScreen(start), toScreen(end), width)
    }

    fun drawCircle(circle: Circle3D) {
        val ratio = getIsometricRatio()
        val width = circle.radius * (TILE_SIZE_X / 2) * ratio
        val height = circle.radius * (TILE_SIZE_Y / 2) * ratio
        val pos = toScreen(circle.pos)
        shapeDrawer.setColor(circle.color)
        shapeDrawer.ellipse(pos.x, pos.y, width, height)
    }

    private fun drawCircle(worldPos: Vector3, worldRadius: Float, color: Color) {
        val ratio = getIsometricRatio()
        val width = worldRadius * (TILE_SIZE_X / 2) * ratio
        val height = worldRadius * (TILE_SIZE_Y / 2) * ratio
        val pos = toScreen(worldPos)
        shapeDrawer.setColor(color)
        shapeDrawer.ellipse(pos.x, pos.y, width, height)
    }

    /**
     * Draws a 2D projection of a 3D isometric sphere.
     */
    fun drawSphere(sphere: Sphere3D) {
        val referencePointColor = Color(sphere.color.r, sphere.color.g, sphere.color.b, 0.6f)
        val pos = sphere.pos
        val radius = sphere.radius
        // center point of sphere
        drawPoint(pos, 2f, referencePointColor)
        // top point of sphere
        drawPoint(Vector3(pos.x, pos.y, pos.z + radius), 2f, referencePointColor)

        val referenceLineColor = Color(referencePointColor)
        referenceLineColor.a = 0.5f
        // y-axis reference line
        drawLine(
            Vector3(pos.x, pos.y - radius, pos.z),
            Vector3(pos.x, pos.y + radius, pos.z),
            referenceLineColor
        )

        // x-axis reference line
        drawLine(
            Vector3(pos.x - radius, pos.y, pos.z),
            Vector3(pos.x + radius, pos.y, pos.z),
            referenceLineColor
        )

        // z-axis reference line
        drawLine(
            Vector3(pos.x, pos.y, pos.z - radius),
            Vector3(pos.x, pos.y, pos.z + radius),
            referenceLineColor
        )

        // 2D reference circle
        drawCircle(pos, radius, referenceLineColor)

        val screenPos = toScreen(pos)
        val ratio = getIsometricRatio()
        val width = radius * (TILE_SIZE_X / 2) * ratio
        // by adding half tile size, the ellipse will fully cover all the 3D space within the iso projection.
        // note that since the z-axis translates 1:1 to the screen's y-axis, we don't have to worry about using a ratio.
        val height = radius * TILE_SIZE_Z + (TILE_SIZE_Z / 2f)
        shapeDrawer.setColor(sphere.color)
        shapeDrawer.ellipse(screenPos.x, screenPos.y, width, height)
    }
}
