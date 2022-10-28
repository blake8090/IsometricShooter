package bke.iso.v2.engine

import bke.iso.v2.app.service.Service
import bke.iso.v2.engine.assets.Assets
import bke.iso.v2.engine.world.Location
import bke.iso.v2.engine.world.Sprite
import bke.iso.v2.engine.world.Tile
import bke.iso.v2.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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
        world.forEachEntity { _, entities ->
            entities.forEach(this::drawEntity)
        }
        batch.end()
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
}
