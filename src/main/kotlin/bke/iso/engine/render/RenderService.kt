package bke.iso.engine.render

import bke.iso.service.Singleton
import bke.iso.engine.TileService
import bke.iso.engine.asset.AssetService
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.math.toScreen
import bke.iso.engine.math.toVector2
import bke.iso.engine.math.toWorld
import bke.iso.engine.physics.CollisionService
import bke.iso.engine.render.debug.DebugRenderService
import bke.iso.engine.render.shape.DefaultShapeUtil
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.max

@Singleton
class RenderService(
    private val assetService: AssetService,
    private val tileService: TileService,
    private val entityService: EntityService,
    private val collisionService: CollisionService,
    private val eventService: EventService,
    private val debugRenderService: DebugRenderService
) {

    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)
    private val shapeUtil = DefaultShapeUtil(camera)

    private var debugMode = false

    private val shadowSprite = Sprite("shadow", 16f, 16f)

    fun toggleDebugMode() {
        debugMode = !debugMode
    }

    fun setCameraPos(worldPos: Vector3) {
        val pos = toScreen(worldPos)
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun unproject(screenCoords: Vector2): Vector3 {
        val screenPos = camera.unproject(Vector3(screenCoords.x, screenCoords.y, 0f))
        return toWorld(screenPos.toVector2())
    }

    fun setCursor(textureName: String) {
        val texture = assetService.get<Texture>(textureName) ?: return
        val xHotspot = texture.width / 2
        val yHotspot = texture.height / 2
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot))
        pixmap.dispose()
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined
        shapeUtil.update()

        val maxZ = max(entityService.layerCount(), tileService.layerCount())
        var z = 0
        while (z <= maxZ) {
            batch.begin()
            tileService.forEachTileInLayer(z) { location, tile ->
                drawSprite(tile.sprite, location.toVector3())
            }
            entityService.getAllInLayer(z).forEach(this::drawEntity)
            batch.end()
            z++
        }

        if (debugMode) {
            renderDebugMode()
        }
    }

    fun dispose() {
        batch.dispose()
        shapeUtil.dispose()
    }

    private fun drawEntity(entity: Entity) {
        val sprite = entity.get<Sprite>() ?: return
        if (entity.z > 0f) {
            drawSprite(shadowSprite, Vector3(entity.x, entity.y, 0f))
        }
        drawSprite(sprite, Vector3(entity.x, entity.y, entity.z))
        eventService.fire(DrawEntityEvent(entity, batch))
    }

    private fun drawSprite(sprite: Sprite, worldPos: Vector3) {
        val texture = assetService.get<Texture>(sprite.texture) ?: return
        val screenPos = toScreen(worldPos)
            .sub(sprite.offsetX, sprite.offsetY)
        batch.draw(texture, screenPos.x, screenPos.y)
    }

    private fun renderDebugMode() {
        tileService.forEachTile { location, _ ->
            debugRenderService.addPoint(location.toVector3(), 1f, Color.CYAN)
        }

        for (entity in entityService.getAll()) {
            debugRenderService.addPoint(Vector3(entity.x, entity.y, entity.z), 2f, Color.RED)

            collisionService.findCollisionData(entity)?.let { collisionData ->
                debugRenderService.addRectangle(collisionData.box, 1f, Color.GREEN)
            }

            if (entity.z != 0f) {
                val start = Vector3(entity.x, entity.y, 0f)
                val end = Vector3(entity.x, entity.y, entity.z)
                debugRenderService.addPoint(start, 2f, Color.RED)
                debugRenderService.addLine(start, end, 1f, Color.PURPLE)
            }
        }

        debugRenderService.render(shapeUtil)
    }
}
