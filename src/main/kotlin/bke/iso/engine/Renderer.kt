package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.engine.assets.Assets
import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.Sprite
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

@Service
class Renderer(
    private val entities: Entities,
    private val tiles: Tiles,
    private val units: Units,
    private val assets: Assets
) {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1280f, 720f)
    private val debugRenderer = DebugRenderer(units, tiles, entities)

    fun setCameraPos(x: Float, y: Float) {
        val screenPos = units.worldToScreen(x, y)
        camera.position.x = screenPos.x
        camera.position.y = screenPos.y
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined

        renderWorld()
        debugRenderer.render(camera)
    }

    private fun renderWorld() {
        batch.begin()
        tiles.forEachTile { location, tile ->
            drawTile(tile, location)
        }

        for (entity in entities) {
            drawEntity(entity)
        }
        batch.end()
    }

    private fun drawTile(tile: Tile, location: Location) {
        val screenPos = units.worldToScreen(location)
        drawSprite(tile.sprite, screenPos)
    }

    private fun drawEntity(entity: Entity) {
        val pos = units.worldToScreen(entity.getPos())
        val component = entity.getComponent<Sprite>()
        val sprite = component ?: return
        drawSprite(sprite, pos)
    }

    private fun drawSprite(sprite: Sprite, pos: Vector2) {
        val texture = assets.get<Texture>(sprite.texture) ?: return
        val finalPos = pos.sub(sprite.offset)
        batch.draw(texture, finalPos.x, finalPos.y)
    }
}

private class DebugRenderer(
    private val units: Units,
    private val tiles: Tiles,
    private val entities: Entities
) {
    private val shapeRenderer = ShapeRenderer()

    fun render(camera: OrthographicCamera) {
        shapeRenderer.projectionMatrix = camera.combined

        tiles.forEachTile { location, _ ->
            val screenPos = units.worldToScreen(location)
            drawCircle(
                screenPos.x,
                screenPos.y,
                3f,
                Color.CYAN
            )
        }

        for (entity in entities) {
            val pos = entity.getPos()
            drawWorldCircle(pos.x, pos.y, 3f, Color.RED)
            drawCollisionBox(entity)
        }
    }

    private fun drawCollisionBox(entity: Entity) {
        val collisionBox = entity.getComponent<Collision>()
            ?.box
            ?: return
        val pos = entity.getPos()
        drawWorldRectangle(
            pos.x + collisionBox.x,
            pos.y + collisionBox.y,
            collisionBox.width,
            collisionBox.length,
            Color.GREEN
        )
    }

    private fun drawCircle(
        x: Float,
        y: Float,
        size: Float,
        color: Color
    ) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(x, y, size)
        shapeRenderer.end()
    }

    private fun drawWorldCircle(
        x: Float,
        y: Float,
        size: Float,
        color: Color
    ) {
        val screenPos = units.worldToScreen(x, y)
        drawCircle(screenPos.x, screenPos.y, size, color)
    }

    private fun drawRectangle(
        bottomLeft: Vector2,
        bottomRight: Vector2,
        topLeft: Vector2,
        topRight: Vector2,
        color: Color
    ) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }

    private fun drawRectangle(
        x: Float,
        y: Float,
        width: Float,
        length: Float,
        color: Color
    ) {
        val bottomLeft = Vector2(x, y)
        val bottomRight = Vector2(x + width, y)
        val topLeft = Vector2(x, y + length)
        val topRight = Vector2(x + width, y + length)
        drawRectangle(bottomLeft, bottomRight, topLeft, topRight, color)
    }

    private fun drawWorldRectangle(
        x: Float,
        y: Float,
        width: Float,
        length: Float,
        color: Color
    ) {
        val bottomLeft = Vector2(x, y)
        val bottomRight = Vector2(x + width, y)
        val topLeft = Vector2(x, y + length)
        val topRight = Vector2(x + width, y + length)
        drawRectangle(
            units.worldToScreen(bottomLeft),
            units.worldToScreen(bottomRight),
            units.worldToScreen(topLeft),
            units.worldToScreen(topRight),
            color
        )
    }
}
