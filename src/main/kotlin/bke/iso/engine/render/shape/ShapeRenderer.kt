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
import mu.KotlinLogging
import space.earlygrey.shapedrawer.ShapeDrawer

class ShapeRenderer(batch: PolygonSpriteBatch) {

    private val log = KotlinLogging.logger {}

    private val shapeDrawer: ShapeDrawer

    init {
        val pixel = makePixelTexture()
        val region = TextureRegion(pixel, 0, 0, 1, 1)
        shapeDrawer = ShapeDrawer(batch, region)
    }

    fun update() {
        shapeDrawer.update()
    }

    fun draw(shapeArray: ShapeArray) {
        shapeDrawer.batch.begin()
        for (shape in shapeArray) {
            drawShape(shape)
        }
        shapeDrawer.batch.end()
    }

    private fun drawShape(shape: Shape) =
        when (shape) {
            is Line3D -> drawLine(shape.start, shape.end, shape.color, shape.width)
            is Circle3D -> drawCircle(shape.pos, shape.radius, shape.color)
            is Point3D -> drawPoint(shape.pos, shape.size, shape.color)
            is Sphere3D -> drawSphere(shape)
            is Rectangle2D -> drawRectangle(shape)
        }

    private fun drawPoint(worldPos: Vector3, size: Float, color: Color) {
        shapeDrawer.filledCircle(toScreen(worldPos), size, color)
    }

    private fun drawRectangle(rectangle: Rectangle2D) {
        shapeDrawer.setColor(rectangle.color)
        shapeDrawer.rectangle(
            rectangle.pos.x,
            rectangle.pos.y,
            rectangle.size.x,
            rectangle.size.y,
            rectangle.lineWidth
        )
    }

    private fun drawLine(start: Vector3, end: Vector3, color: Color, width: Float = 1f) {
        shapeDrawer.setColor(color)
        shapeDrawer.line(toScreen(start), toScreen(end), width)
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

    fun dispose() {
        shapeDrawer.region.texture.dispose()
        log.info { "Disposed shapeDrawer" }
    }
}
