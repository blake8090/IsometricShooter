package bke.iso.v2.engine.render

import bke.iso.engine.TILE_HEIGHT
import bke.iso.engine.TILE_WIDTH
import bke.iso.engine.getIsometricRatio
import bke.iso.service.Singleton
import bke.iso.v2.engine.TileService
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.event.EventService
import bke.iso.v2.engine.math.toScreen
import bke.iso.v2.engine.physics.CollisionData
import bke.iso.v2.engine.physics.CollisionService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
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

    fun toggleDebugMode() {
        debugMode = !debugMode
    }

    fun setCameraPos(pos: Vector2) {
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun unproject(pos: Vector2): Vector3 =
        camera.unproject(Vector3(pos.x, pos.y, 0f))

    fun setCursor(textureName: String) {
        val texture = assetService.get<Texture>(textureName) ?: return
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, 0, 0))
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
            drawSprite(tile.sprite, toScreen(location.x.toFloat(), location.y.toFloat()))
        }
        batch.end()

        batch.begin()
        entityService.getAll().forEach(this::drawEntity)
        batch.end()

        if (debugMode) {
            renderDebugMode()
        }
    }

    private fun drawEntity(entity: Entity) {
        val sprite = entity.get<Sprite>() ?: return
        val pos = toScreen(entity.x, entity.y)
        drawSprite(sprite, pos)
        eventService.fire(DrawEntityEvent(entity, batch))
    }

    private fun drawSprite(sprite: Sprite, pos: Vector2) {
        val texture = assetService.get<Texture>(sprite.texture) ?: return
        val offsetPos = Vector2(
            pos.x - sprite.offsetX,
            pos.y - sprite.offsetY,
        )
        batch.draw(texture, offsetPos.x, offsetPos.y)
    }

    private fun renderDebugMode() {
        tileService.forEachTile { location, _ ->
            val markerPos = toScreen(location.toVector2())
            drawPoint(markerPos, 1f, Color.CYAN)
        }

        for (entity in entityService.getAll()) {
            val markerPos = toScreen(entity.x, entity.y)
            drawPoint(markerPos, 2f, Color.RED)
            drawCollisionBoxes(entity)
            drawDebugData(entity)
            entity.remove<DebugData>()
        }
    }

    private fun drawCollisionBoxes(entity: Entity) {
        collisionService.findCollisionData(entity)
            ?.let(CollisionData::box)
            ?.let(::toScreen)
            ?.let { polygon -> drawPolygon(polygon, Color.GREEN) }
            ?: return
    }

    private fun drawDebugData(entity: Entity) {
        val debugData = entity.get<DebugData>() ?: return

        for (line in debugData.lines) {
            drawLine(toScreen(line.start), toScreen(line.end), line.color)
        }

        for (circle in debugData.circles) {
            val radius = circle.radius
            val ratio = getIsometricRatio()
            val width = radius * TILE_WIDTH * ratio
            val height = radius * TILE_HEIGHT * ratio
            val pos = toScreen(entity.x, entity.y)
            drawEllipse(pos, width, height, circle.color)
        }

        for (point in debugData.points) {
            drawPoint(toScreen(point.pos), point.size, point.color)
        }
    }

    private fun drawPoint(pos: Vector2, size: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(pos.x, pos.y, size)
        shapeRenderer.end()
    }

    private fun drawPolygon(polygon: Polygon, color: Color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.polygon(polygon.transformedVertices)
        shapeRenderer.end()
    }

    private fun drawLine(start: Vector2, end: Vector2, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.line(start, end)
        shapeRenderer.end()
    }

    private fun drawEllipse(pos: Vector2, width: Float, height: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.ellipse(pos.x - (width/2), pos.y - (height/2), width, height)
        shapeRenderer.end()
    }
}
