package bke.iso.engine.render.shape

import bke.iso.engine.math.TILE_SIZE_X
import bke.iso.engine.math.TILE_SIZE_Y
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

    override fun dispose() {
        texture.dispose()
    }
}
