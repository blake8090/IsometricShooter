package bke.iso.engine.render.shape

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

class ShapeDrawerUtil(batch: PolygonSpriteBatch) : ShapeUtil {

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

    override fun update() {
        shapeDrawer.update()
    }

    override fun begin() {
        shapeDrawer.batch.begin()
    }

    override fun end() {
        shapeDrawer.batch.end()
    }

    override fun drawPoint(worldPos: Vector3, size: Float, color: Color) {
        val pos = toScreen(worldPos)
        shapeDrawer.filledCircle(pos, size, color)
    }

    override fun drawRectangle(worldRect: Rectangle, color: Color) {
        val polygon = toScreen(worldRect)
        shapeDrawer.setColor(color)
        shapeDrawer.polygon(polygon)
    }

    override fun drawLine(start: Vector3, end: Vector3, color: Color) {
        val startScreen = toScreen(start)
        val endScreen = toScreen(end)
        shapeDrawer.setColor(color)
        shapeDrawer.line(startScreen, endScreen)
    }

    override fun drawCircle(worldPos: Vector3, worldRadius: Float, color: Color) {
        val ratio = getIsometricRatio()
        val width = worldRadius * (TILE_SIZE_X / 2) * ratio
        val height = worldRadius * (TILE_SIZE_Y / 2) * ratio
        val pos = toScreen(worldPos)
        shapeDrawer.setColor(color)
        shapeDrawer.ellipse(pos.x, pos.y, width, height)
    }

    override fun drawBox(worldPos: Vector3, dimensions: Vector3, color: Color) {
        val rect = Rectangle(worldPos.x, worldPos.y, dimensions.x, dimensions.y)

        val bottomPolygon = toScreen(rect, worldPos.z)
        shapeDrawer.setColor(color)
        shapeDrawer.polygon(bottomPolygon)

        if (dimensions.z > 0) {
            val topPolygon = toScreen(rect, worldPos.z + dimensions.z)
            shapeDrawer.polygon(topPolygon)

            // bottom left
            shapeDrawer.line(
                toScreen(
                    worldPos.x,
                    worldPos.y,
                    worldPos.z
                ),
                toScreen(
                    worldPos.x,
                    worldPos.y,
                    worldPos.z + dimensions.z
                )
            )

            // bottom right
            shapeDrawer.line(
                toScreen(
                    worldPos.x + dimensions.x,
                    worldPos.y,
                    worldPos.z
                ),
                toScreen(
                    worldPos.x + dimensions.x,
                    worldPos.y,
                    worldPos.z + dimensions.z
                )
            )

            // top left
            shapeDrawer.line(
                toScreen(
                    worldPos.x,
                    worldPos.y + dimensions.y,
                    worldPos.z
                ),
                toScreen(
                    worldPos.x,
                    worldPos.y + dimensions.y,
                    worldPos.z + dimensions.z
                )
            )

            // top right
            shapeDrawer.line(
                toScreen(
                    worldPos.x + dimensions.x,
                    worldPos.y + dimensions.y,
                    worldPos.z
                ),
                toScreen(
                    worldPos.x + dimensions.x,
                    worldPos.y + dimensions.y,
                    worldPos.z + dimensions.z
                )
            )
        }
    }

    override fun drawSphere(worldPos: Vector3, worldRadius: Float, color: Color) {
        val referencePointColor = Color(color.r, color.g, color.b, 0.6f)
        // center point of sphere
        drawPoint(worldPos, 2f, referencePointColor)
        // top point of sphere
        drawPoint(Vector3(worldPos.x, worldPos.y, worldPos.z + worldRadius), 2f, referencePointColor)

        val referenceLineColor = Color(color.r, color.g, color.b, 0.5f)
        // y-axis reference line
        drawLine(
            Vector3(
                worldPos.x,
                worldPos.y - worldRadius,
                worldPos.z
            ),
            Vector3(
                worldPos.x,
                worldPos.y + worldRadius,
                worldPos.z
            ),
            referenceLineColor
        )

        // x-axis reference line
        drawLine(
            Vector3(
                worldPos.x - worldRadius,
                worldPos.y,
                worldPos.z
            ),
            Vector3(
                worldPos.x + worldRadius,
                worldPos.y,
                worldPos.z
            ),
            referenceLineColor
        )

        // z-axis reference line
        drawLine(
            Vector3(
                worldPos.x,
                worldPos.y,
                worldPos.z - worldRadius
            ),
            Vector3(
                worldPos.x,
                worldPos.y,
                worldPos.z + worldRadius
            ),
            referenceLineColor
        )

        // 2D reference circle
        drawCircle(worldPos, worldRadius, referenceLineColor)

        val pos = toScreen(worldPos)
        val ratio = getIsometricRatio()
        val width = worldRadius * (TILE_SIZE_X / 2) * ratio
        // by adding half tile size, the ellipse will fully cover all the 3D space within the iso projection.
        // note that since the z-axis translates 1:1 to the screen's y-axis, we don't have to worry about using a ratio.
        val height = worldRadius * TILE_SIZE_Z + (TILE_SIZE_Z / 2f)
        shapeDrawer.setColor(color)
        shapeDrawer.ellipse(pos.x, pos.y, width, height)
    }

    override fun dispose() {
        texture.dispose()
    }
}
