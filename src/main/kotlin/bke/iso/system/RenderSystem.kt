package bke.iso.system

import bke.iso.asset.AssetService
import bke.iso.world.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class RenderSystem(
    private val assetService: AssetService,
    private val world: World
) : System() {
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val offset = Vector2(200f, 200f)

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        drawMap()

        val location = Location(0, 0)
        val start = world.worldToScreen(location, offset)
        val end = world.worldToScreen(Location(1, 0), offset)
        drawLine(start, end, 1, Color.GREEN)
    }

    private fun drawMap() {
        val data = world.getAllDataByLocation()[0]!!
        // drawing one y line

        // draw tiles && entities
        batch.begin()

        for ((location, tile, entities) in data) {
            tile?.let { drawTile(tile, location) }
            entities.forEach(this::drawEntity)
        }
        batch.end()
    }

    private fun drawTile(tile: Tile, location: Location) {
        val texture = assetService.getAsset(tile.texture, Texture::class) ?: return
        val pos = world.worldToScreen(location, offset)
        batch.draw(texture, pos.x, pos.y)
    }

    private fun drawEntity(id: Int) {
        val positionComponent = world.getEntityComponent(id, PositionComponent::class) ?: return
        val textureComponent = world.getEntityComponent(id, TextureComponent::class) ?: return
        val texture = assetService.getAsset(textureComponent.name, Texture::class) ?: return

        val screenPos = world.worldToScreen(Vector2(positionComponent.x, positionComponent.y), offset)
        batch.draw(texture, screenPos.x, screenPos.y)
    }

    private fun drawLine(start: Vector2, end: Vector2, width: Int, color: Color) {
        Gdx.gl.glLineWidth(width.toFloat())
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.line(start, end)
        shapeRenderer.end()
        // resets line width to default
        Gdx.gl.glLineWidth(1f)
    }
}

//fun DrawDebugLine(start: Vector2?, end: Vector2?, lineWidth: Int, color: Color?, projectionMatrix: Matrix4?) {
//    Gdx.gl.glLineWidth(lineWidth.toFloat())
//    debugRenderer.projectionMatrix = projectionMatrix
//    debugRenderer.begin(ShapeRenderer.ShapeType.Line)
//    debugRenderer.color = color
//    debugRenderer.line(start, end)
//    debugRenderer.end()
//    Gdx.gl.glLineWidth(1f)
//}
//
//fun DrawDebugLine(start: Vector2?, end: Vector2?, projectionMatrix: Matrix4?) {
//    Gdx.gl.glLineWidth(2f)
//    debugRenderer.projectionMatrix = projectionMatrix
//    debugRenderer.begin(ShapeRenderer.ShapeType.Line)
//    debugRenderer.color = Color.WHITE
//    debugRenderer.line(start, end)
//    debugRenderer.end()
//    Gdx.gl.glLineWidth(1f)
//}
