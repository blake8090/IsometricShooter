package bke.iso.engine.render

import bke.iso.app.service.Service
import bke.iso.engine.*
import bke.iso.engine.assets.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

@Service
class Renderer(
    private val entityService: EntityService,
    private val tiles: Tiles,
    private val assets: Assets,
    private val input: Input,
    private val debugRenderer: DebugRenderer
) {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1280f, 720f)
    private var debugEnabled = true

//    var mouseCursor: Sprite? = null

    fun setCameraPos(pos: Vector2) {
        val screenPos = Units.worldToScreen(pos)
        camera.position.x = screenPos.x
        camera.position.y = screenPos.y
    }

    fun toggleDebug() {
        debugEnabled = !debugEnabled
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined

        batch.begin()
        renderWorld()
        batch.end()

        if (debugEnabled) {
            debugRenderer.render(camera)
        }

        batch.begin()
        drawMouseCursor()
        batch.end()
    }

    private fun renderWorld() {
        tiles.forEachTile { location, tile ->
            drawSprite(tile.sprite, Units.worldToScreen(location))
        }
        entityService.getAll()
            .forEach(this::drawEntity)
    }

    private fun drawEntity(entity: Entity) {
        val sprite = entity.get<Sprite>() ?: return
        val pos = Units.worldToScreen(entity.x, entity.y)
        drawSprite(sprite, pos)
    }

    private fun drawSprite(sprite: Sprite, pos: Vector2) {
        val texture = assets.get<Texture>(sprite.texture) ?: return
        val offsetPos = Vector2(
            pos.x - sprite.offsetX,
            pos.y - sprite.offsetY,
        )
        batch.draw(texture, offsetPos.x, offsetPos.y)
    }

    // TODO: should the Renderer own this method?
    fun unproject(pos: Vector2): Vector3 =
        camera.unproject(Vector3(pos.x, pos.y, 0f))

    private fun drawMouseCursor() {
//        mouseCursor
//            ?.let { sprite ->
//                val mousePos = input.getMousePos()
//                val pos = unproject(mousePos)
//                drawSprite(sprite, Vector2(pos.x, pos.y))
//            }
//            ?: return
    }
}
