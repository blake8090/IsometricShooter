package bke.iso.engine.render

import bke.iso.engine.asset.AssetService
import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventService
import bke.iso.engine.math.toScreen
import bke.iso.engine.math.toVector2
import bke.iso.engine.math.toWorld
import bke.iso.engine.physics.collision.CollisionServiceV2
import bke.iso.engine.render.debug.DebugRenderService
import bke.iso.engine.render.shape.ShapeDrawerUtil
import bke.iso.engine.world.Tile
import bke.iso.engine.world.WorldObject
import bke.iso.engine.world.WorldService
import bke.iso.service.v2.SingletonService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class RenderService(
    private val assetService: AssetService,
    private val worldService: WorldService,
    private val collisionServiceV2: CollisionServiceV2,
    private val eventService: EventService,
    private val debugRenderService: DebugRenderService
) : SingletonService {

    private val batch = PolygonSpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)
    private val shapeUtil = ShapeDrawerUtil(batch)

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

        batch.begin()
        val sortedObjects = worldService.getAllObjects()
            .sortedWith(
                compareBy(WorldObject::layer)
                    .thenByDescending { it.y - it.x }
                    .thenBy(WorldObject::z)
            )

        sortedObjects.forEach { worldObject ->
            when (worldObject) {
                is Entity -> drawEntity(worldObject)
                is Tile -> drawTile(worldObject)
            }
        }
        batch.end()

        if (debugMode) {
            debugRenderService.render(shapeUtil)
        }
        // debug data still accumulates even when not in debug mode!
        debugRenderService.clear()
    }

    override fun dispose() {
        batch.dispose()
        shapeUtil.dispose()
    }

    private fun drawEntity(entity: Entity) {
        val sprite = entity.get<Sprite>() ?: return
        if (entity.z > 0f && entity.has<DrawShadow>()) {
            drawSprite(shadowSprite, Vector3(entity.x, entity.y, 0f))
        }
        drawSprite(sprite, Vector3(entity.x, entity.y, entity.z))
        eventService.fire(DrawEntityEvent(entity, batch))
        addEntityDebugData(entity)
    }

    private fun addEntityDebugData(entity: Entity) {
        debugRenderService.addPoint(Vector3(entity.x, entity.y, entity.z), 2f, Color.RED)

        collisionServiceV2.findCollisionData(entity)?.let { data ->
            debugRenderService.addBox(data.box, Color.GREEN)
        }

        if (entity.z != 0f) {
            val start = Vector3(entity.x, entity.y, 0f)
            val end = Vector3(entity.x, entity.y, entity.z)
            debugRenderService.addPoint(start, 2f, Color.RED)
            debugRenderService.addLine(start, end, 1f, Color.PURPLE)
        }
    }

    private fun drawTile(tile: Tile) {
        drawSprite(tile.sprite, Vector3(tile.x, tile.y, tile.z))
    }

    private fun drawSprite(sprite: Sprite, worldPos: Vector3) {
        val texture = assetService.get<Texture>(sprite.texture) ?: return
        val screenPos = toScreen(worldPos)
            .sub(sprite.offsetX, sprite.offsetY)
        batch.draw(texture, screenPos.x, screenPos.y)
    }
}
