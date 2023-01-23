package bke.iso.engine.render

import bke.iso.service.Singleton
import bke.iso.engine.TileService
import bke.iso.engine.asset.AssetService
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.math.TILE_HEIGHT
import bke.iso.engine.math.TILE_WIDTH
import bke.iso.engine.math.getIsometricRatio
import bke.iso.engine.math.toScreen
import bke.iso.engine.math.toVector2
import bke.iso.engine.math.toWorld
import bke.iso.engine.physics.CollisionService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

@Singleton
class RenderService(
    private val assetService: AssetService,
    private val tileService: TileService,
    private val entityService: EntityService,
    private val collisionService: CollisionService,
    private val eventService: EventService
) {

    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera(1920f, 1080f)

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
        shapeRenderer.projectionMatrix = camera.combined

        batch.begin()
        tileService.forEachTile { location, tile ->
            drawSprite(tile.sprite, location.toVector3())
        }
        batch.end()

        batch.begin()
        entityService.getAll().forEach(this::drawEntity)
        batch.end()

        if (debugMode) {
            renderDebugMode()
        }
    }

    fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
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
            drawPoint(location.toVector3(), 1f, Color.CYAN)
        }

        for (entity in entityService.getAll()) {
            drawPoint(Vector3(entity.x, entity.y, entity.z), 2f, Color.RED)
            drawCollisionBoxes(entity)
            drawDebugData(entity)
            entity.remove<DebugData>()

            if (entity.z != 0f) {
                val start = Vector3(entity.x, entity.y, entity.z)
                val end = Vector3(entity.x, entity.y, 0f)
                drawPoint(end, 2f, Color.RED)
                drawLine(start, end, Color.PURPLE)
            }
        }
    }

    private fun drawCollisionBoxes(entity: Entity) {
        collisionService.findCollisionData(entity)
            ?.let { collisionData -> drawRectangle(collisionData.box, Color.GREEN) }
            ?: return
    }

    private fun drawDebugData(entity: Entity) {
        val debugData = entity.get<DebugData>() ?: return

        for (line in debugData.lines) {
            drawLine(line.start, line.end, line.color)
        }

        for (circle in debugData.circles) {
            drawCircle(Vector3(entity.x, entity.y, entity.z), circle.radius, circle.color)
        }

        for (point in debugData.points) {
            drawPoint(point.pos, point.size, point.color)
        }
    }

    private fun drawPoint(worldPos: Vector3, size: Float, color: Color) {
        val pos = toScreen(worldPos)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(pos.x, pos.y, size)
        shapeRenderer.end()
    }

    private fun drawRectangle(worldRect: Rectangle, color: Color) {
        val polygon = toScreen(worldRect)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.polygon(polygon.transformedVertices)
        shapeRenderer.end()
    }

    private fun drawLine(start: Vector3, end: Vector3, color: Color) {
        val startScreen = toScreen(start)
        val endScreen = toScreen(end)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.line(startScreen, endScreen)
        shapeRenderer.end()
    }

    private fun drawCircle(worldPos: Vector3, worldRadius: Float, color: Color) {
        val ratio = getIsometricRatio()
        val width = worldRadius * TILE_WIDTH * ratio
        val height = worldRadius * TILE_HEIGHT * ratio
        val pos = toScreen(worldPos)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.ellipse(pos.x - (width / 2), pos.y - (height / 2), width, height)
        shapeRenderer.end()
    }
}
