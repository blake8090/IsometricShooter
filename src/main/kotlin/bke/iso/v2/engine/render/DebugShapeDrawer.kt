package bke.iso.v2.engine.render

import bke.iso.engine.math.TILE_SIZE_X
import bke.iso.engine.math.TILE_SIZE_Y
import bke.iso.engine.math.TILE_SIZE_Z
import bke.iso.engine.math.getIsometricRatio
import bke.iso.engine.math.toScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import space.earlygrey.shapedrawer.ShapeDrawer

class DebugShapeDrawer(batch: PolygonSpriteBatch) {

    private val shapeDrawer: ShapeDrawer
    private val texture: Texture

    init {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        texture = Texture(pixmap)
        pixmap.dispose()
        val region = TextureRegion(texture, 0, 0, 1, 1)
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

    fun dispose() {
        texture.dispose()
    }

    fun drawPoint(point: DebugPoint) =
        drawPoint(point.pos, point.size, point.color)

    private fun drawPoint(worldPos: Vector3, size: Float, color: Color) =
        shapeDrawer.filledCircle(toScreen(worldPos), size, color)

    fun drawRectangle(rectangle: DebugRectangle) {
        val polygon = toScreen(rectangle.rectangle)
        shapeDrawer.setColor(rectangle.color)
        shapeDrawer.polygon(polygon)
    }

    fun drawLine(line: DebugLine) {
        drawLine(line.start, line.end, line.color, line.width)
    }

    private fun drawLine(start: Vector3, end: Vector3, color: Color, width: Float = 1f) {
        shapeDrawer.setColor(color)
        shapeDrawer.line(toScreen(start), toScreen(end), width)
    }

    fun drawCircle(circle: DebugCircle) {
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

    // TODO: use segments
    fun drawBox(box: DebugBox) {
        val pos = box.pos
        val dim = box.dimensions
        val rect = Rectangle(pos.x, pos.y, dim.x, dim.y)

        val bottomPolygon = toScreen(rect, pos.z)
        shapeDrawer.setColor(box.color)
        shapeDrawer.polygon(bottomPolygon)

        if (dim.z > 0) {
            val topPolygon = toScreen(rect, pos.z + dim.z)
            shapeDrawer.polygon(topPolygon)

            // bottom left
            shapeDrawer.line(
                toScreen(pos.x, pos.y, pos.z),
                toScreen(pos.x, pos.y, pos.z + dim.z)
            )

            // bottom right
            shapeDrawer.line(
                toScreen(pos.x + dim.x, pos.y, pos.z),
                toScreen(pos.x + dim.x, pos.y, pos.z + dim.z)
            )

            // top left
            shapeDrawer.line(
                toScreen(pos.x, pos.y + dim.y, pos.z),
                toScreen(pos.x, pos.y + dim.y, pos.z + dim.z)
            )

            // top right
            shapeDrawer.line(
                toScreen(pos.x + dim.x, pos.y + dim.y, pos.z),
                toScreen(pos.x + dim.x, pos.y + dim.y, pos.z + dim.z)
            )
        }
    }

    /**
     * Draws a 2D projection of a 3D isometric sphere.
     */
    fun drawSphere(sphere: DebugSphere) {
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
