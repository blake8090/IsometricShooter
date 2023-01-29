package bke.iso.engine.render

import bke.iso.engine.math.TILE_SIZE_X
import bke.iso.engine.math.TILE_SIZE_Y
import bke.iso.engine.math.getIsometricRatio
import bke.iso.engine.math.toScreen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3

class ShapeRenderHelper {

    private val shapeRenderer = ShapeRenderer()

    fun update(camera: Camera) {
        shapeRenderer.projectionMatrix = camera.combined
    }

    fun drawPoint(worldPos: Vector3, size: Float, color: Color) {
        val pos = toScreen(worldPos)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(pos.x, pos.y, size)
        shapeRenderer.end()
    }

    fun drawRectangle(worldRect: Rectangle, color: Color) {
        val polygon = toScreen(worldRect)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.polygon(polygon.transformedVertices)
        shapeRenderer.end()
    }

    fun drawLine(start: Vector3, end: Vector3, color: Color) {
        val startScreen = toScreen(start)
        val endScreen = toScreen(end)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.line(startScreen, endScreen)
        shapeRenderer.end()
    }

    fun drawCircle(worldPos: Vector3, worldRadius: Float, color: Color) {
        val ratio = getIsometricRatio()
        val width = worldRadius * TILE_SIZE_X * ratio
        val height = worldRadius * TILE_SIZE_Y * ratio
        val pos = toScreen(worldPos)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.ellipse(pos.x - (width / 2), pos.y - (height / 2), width, height)
        shapeRenderer.end()
    }

    fun dispose() {
        shapeRenderer.dispose()
    }
}
