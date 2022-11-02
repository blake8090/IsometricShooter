package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.engine.assets.Assets
import bke.iso.engine.world.*
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
        val screenPos = world.units.worldToScreen(location)
        drawSprite(tile.sprite, screenPos)
    }

    private fun drawEntity(id: UUID) {
        val pos = world.entities.getPos(id)
            ?.let { world.units.worldToScreen(it) }
            ?: return
        val sprite = world.entities.getComponent(id, Sprite::class) ?: return
        drawSprite(sprite, pos)
    }

    private fun drawSprite(sprite: Sprite, pos: Vector2) {
        val texture = assets.get<Texture>(sprite.texture) ?: return
        val finalPos = pos.sub(sprite.offset)
        batch.draw(texture, finalPos.x, finalPos.y)
    }

    private fun drawEntityDebug(id: UUID) {
        val pos = world.entities.getPos(id)
            ?.let { world.units.worldToScreen(it) }
            ?: return
        drawDebugCircle(pos.x, pos.y, 2f, Color.RED)

        // draw texture bounds
//        val sprite = world.entities.getComponent(id, Sprite::class) ?: return
//        val texture = assets.get<Texture>(sprite.texture) ?: return
//        drawDebugRectangle(
//            pos.x - sprite.offset.x,
//            pos.y - sprite.offset.y,
//            texture.width.toFloat(),
//            texture.height.toFloat(),
//            Color.GREEN
//        )
        drawEntityBounds(id)
        drawCollisionBox(id)
    }

    private fun drawEntityBounds(id: UUID) {
        val pos = world.entities.getPos(id) ?: return
        val bounds = world.entities.getComponent(id, Bounds::class) ?: return

        val bottomLeft = world.units.worldToScreen(pos)
        val bottomRight = world.units.worldToScreen(pos.x + bounds.width, pos.y)
        val topLeft = world.units.worldToScreen(pos.x, pos.y + bounds.length)
        val topRight = world.units.worldToScreen(pos.x + bounds.width, pos.y + bounds.length)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.GREEN
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }

    private fun drawCollisionBox(id: UUID) {
        val pos = world.entities.getPos(id) ?: return
        val collisionBox = world.entities.getComponent(id, CollisionBox::class) ?: return

        val start = Vector2(
            pos.x + collisionBox.x,
            pos.y + collisionBox.y
        )

        val bottomLeft = world.units.worldToScreen(start)
        val bottomRight = world.units
            .worldToScreen(
                start.x + collisionBox.width,
                start.y
            )
        val topLeft = world.units
            .worldToScreen(
                start.x,
                start.y + collisionBox.length
            )
        val topRight = world.units
            .worldToScreen(
                start.x + collisionBox.width,
                start.y + collisionBox.length
            )
//        val topLeft = world.units.worldToScreen(pos.x, pos.y + bounds.length)
//        val topRight = world.units.worldToScreen(pos.x + bounds.width, pos.y + bounds.length)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.GREEN
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }

    private fun drawTileDebug(location: Location) {
        val pos = world.units.worldToScreen(location)
        drawDebugCircle(pos.x, pos.y, 2f, Color.CYAN)
    }

    private fun drawDebugCircle(
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

    private fun drawDebugRectangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color
    ) {
        val topLeft = Vector2(x, y + height)
        val topRight = Vector2(x + width, y + height)
        val bottomLeft = Vector2(x, y)
        val bottomRight = Vector2(x + width, y)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.line(topLeft, topRight)
        shapeRenderer.line(bottomLeft, bottomRight)
        shapeRenderer.line(topLeft, bottomLeft)
        shapeRenderer.line(topRight, bottomRight)
        shapeRenderer.end()
    }
}
