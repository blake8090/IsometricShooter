package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.engine.assets.Assets
import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.Sprite
import bke.iso.engine.input.Input
import bke.iso.engine.physics.Collision
import bke.iso.engine.physics.CollisionProjection
import bke.iso.engine.physics.getCollisionArea
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

@Service
class Renderer(
    private val entities: Entities,
    private val tiles: Tiles,
    private val assets: Assets,
    private val input: Input
) {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1280f, 720f)
    private val debugRenderer = DebugRenderer(tiles, entities)
    private var debugEnabled = false

    var mouseCursor: Sprite? = null

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
            drawTile(tile, location)
        }

        for (entity in entities) {
            drawEntity(entity)
        }
    }

    private fun drawTile(tile: Tile, location: Location) {
        val screenPos = Units.worldToScreen(location)
        drawSprite(tile.sprite, screenPos)
    }

    private fun drawEntity(entity: Entity) {
        val pos = Units.worldToScreen(entity.getPos())
        val component = entity.getComponent<Sprite>()
        val sprite = component ?: return
        drawSprite(sprite, pos)
    }

    private fun drawSprite(sprite: Sprite, pos: Vector2) {
        val texture = assets.get<Texture>(sprite.texture) ?: return
        val finalPos = pos.sub(sprite.offset)
        batch.draw(texture, finalPos.x, finalPos.y)
    }

    // TODO: should the Renderer own this method?
    fun unproject(pos: Vector2): Vector3 =
        camera.unproject(Vector3(pos.x, pos.y, 0f))

    private fun drawMouseCursor() {
        mouseCursor
            ?.let { sprite ->
                val mousePos = input.getMousePos()
                val pos = unproject(mousePos)
                drawSprite(sprite, Vector2(pos.x, pos.y))
            }
            ?: return
    }
}

private class DebugRenderer(
    private val tiles: Tiles,
    private val entities: Entities
) {
    private val shapeRenderer = ShapeRenderer()

    fun render(camera: OrthographicCamera) {
        shapeRenderer.projectionMatrix = camera.combined

        Gdx.gl.glEnable(GL30.GL_BLEND)
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)

        tiles.forEachTile { location, _ ->
            val circle = Circle(location.x.toFloat(), location.y.toFloat(), 3f)
            drawWorldCircle(circle, Color.CYAN)
        }

        for (entity in entities) {
            val circle = Circle(entity.getPos(), 3f)
            drawWorldCircle(circle, Color.RED)
            drawCollisionAreas(entity)
        }

        Gdx.gl.glDisable(GL30.GL_BLEND)
    }

    private fun drawCollisionAreas(entity: Entity) {
        val collision = entity.getComponent<Collision>() ?: return
        val pos = entity.getPos()

        val collisionArea = getCollisionArea(pos, collision.bounds)
        drawWorldRectangle(collisionArea, Color.GREEN)

        val collisionProjection = entity.getComponent<CollisionProjection>() ?: return
        collisionProjection.xProjection
            ?.let { area -> drawWorldRectangle(area, Color.RED) }
        collisionProjection.yProjection
            ?.let { area -> drawWorldRectangle(area, Color.RED) }
    }

    private fun drawWorldCircle(circle: Circle, color: Color) {
        val pos = Units.worldToScreen(circle.x, circle.y)
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(pos.x, pos.y, circle.radius)
        shapeRenderer.end()
    }

    private fun drawWorldRectangle(rectangle: Rectangle, color: Color) {
        val bottomLeft = Units.worldToScreen(
            rectangle.x,
            rectangle.y
        )
        val bottomRight = Units.worldToScreen(
            rectangle.x + rectangle.width,
            rectangle.y
        )
        val topLeft = Units.worldToScreen(
            rectangle.x,
            rectangle.y + rectangle.height
        )
        val topRight = Units.worldToScreen(
            rectangle.x + rectangle.width,
            rectangle.y + rectangle.height
        )

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }
}
