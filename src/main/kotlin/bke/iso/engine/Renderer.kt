package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.engine.assets.Assets
import bke.iso.engine.world.Location
import bke.iso.engine.world.Sprite
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import java.util.UUID

@Service
class Renderer(
    private val world: World,
    private val assets: Assets
) {
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera(1280f, 720f)

    fun setCameraPos(x: Float, y: Float) {
        val screenPos = world.units.worldToScreen(x, y)
        camera.position.x = screenPos.x
        camera.position.y = screenPos.y
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        renderWorld()
    }

    private fun renderWorld() {
        batch.begin()
        world.forEachTile { location, tile ->
            drawTile(tile, location)
        }
        // todo: draw boundaries for tiles, for debugging entity location switching
        world.forEachEntity { _, entities ->
            entities.forEach(this::drawEntity)
        }
        batch.end()

        renderDebugInfo()
    }

    private fun renderDebugInfo() {
        world.forEachTile { location, _ ->
            drawTileDebug(location)
        }
        world.forEachEntity { _, entities ->
            entities.forEach(this::drawEntityDebug)
        }
    }

    private fun drawTile(tile: Tile, location: Location) {
        val texture = assets.get<Texture>(tile.texture) ?: return
        val screenPos = world.units.worldToScreen(location)
        batch.draw(texture, screenPos.x, screenPos.y)
    }

    private fun drawEntity(id: UUID) {
        val pos = world.entities.getPos(id) ?: return
        val sprite = world.entities.getComponent(id, Sprite::class) ?: return
        val texture = assets.get<Texture>(sprite.texture) ?: return
        val screenPos = world.units.worldToScreen(pos.x, pos.y)
        batch.draw(texture, screenPos.x, screenPos.y)
    }

    private fun drawEntityDebug(id: UUID) {
        val pos = world.entities.getPos(id) ?: return
        drawDebugCircle(pos.x, pos.y, 2f, Color.RED)
        drawDebugRectangle(pos.x, pos.y, 1f, 1f)
    }

    private fun drawTileDebug(location: Location) {
        drawDebugCircle(location.x.toFloat(), location.y.toFloat(), 2f, Color.CYAN)
    }

    private fun drawDebugCircle(x: Float, y: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val pos = world.units.worldToScreen(x, y)

        shapeRenderer.circle(pos.x, pos.y, size)
        shapeRenderer.end()
    }

    private fun drawDebugRectangle(x: Float, y: Float, width: Float, height: Float) {
        val topLeft = world.units.worldToScreen(Vector2(x, y + height))
        val topRight = world.units.worldToScreen(Vector2(x + width, y + height))
        val bottomLeft = world.units.worldToScreen(Vector2(x, y))
        val bottomRight = world.units.worldToScreen(Vector2(x + width, y))

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.GREEN
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }
}
