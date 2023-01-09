package bke.iso.v2.engine.render

import bke.iso.service.Singleton
import bke.iso.v2.engine.TileService
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.math.toScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

@Singleton
class RenderService(
    private val assetService: AssetService,
    private val tileService: TileService,
    private val entityService: EntityService
) {

    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)

    fun setCameraPos(pos: Vector2) {
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined

        batch.begin()
        tileService.forEachTile { location, tile ->
            drawSprite(tile.sprite, toScreen(location.x.toFloat(), location.y.toFloat()))
        }
        batch.end()

        batch.begin()
        entityService.getAll().forEach(this::drawEntity)
        batch.end()
    }

    private fun drawEntity(entity: Entity) {
        val sprite = entity.get<Sprite>() ?: return
        val pos = toScreen(entity.x, entity.y)
        drawSprite(sprite, pos)
    }

    private fun drawSprite(sprite: Sprite, pos: Vector2) {
        val texture = assetService.get<Texture>(sprite.texture) ?: return
        val offsetPos = Vector2(
            pos.x - sprite.offsetX,
            pos.y - sprite.offsetY,
        )
        batch.draw(texture, offsetPos.x, offsetPos.y)
    }
}
