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
import com.badlogic.gdx.math.Vector3

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
    }

    private fun drawMap() {
        batch.begin()
        world.forEachLocation { location, tile, ids ->
            tile?.let { drawTile(it, location) }
            ids.forEach(this::drawEntity)
        }
        batch.end()

        drawLine(
            world.getEntityScreenPos(Vector3(0f, 0f, 0f), offset),
            world.getEntityScreenPos(Vector3(1f, 0f, 0f), offset),
            1,
            Color.GREEN
        )

        drawCircle(world.getEntityScreenPos(Vector3(0f, 0f, 0f), offset), 20f, Color.RED)
    }

    private fun drawTile(tile: Tile, location: Location) {
        val texture = assetService.getAsset(tile.texture, Texture::class) ?: return
        val pos = world.locationToScreenPos(location, offset)
        batch.draw(texture, pos.x, pos.y)
    }

    private fun drawEntity(id: Int) {
        val positionComponent = world.getEntityComponent(id, PositionComponent::class) ?: return
        val textureComponent = world.getEntityComponent(id, TextureComponent::class) ?: return
        val texture = assetService.getAsset(textureComponent.name, Texture::class) ?: return

        val screenPos =
            world.getEntityScreenPos(Vector3(positionComponent.x, positionComponent.y, 0f), offset)
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

    private fun drawCircle(pos: Vector2, size: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.circle(pos.x, pos.y, size)
        shapeRenderer.end()
    }
}
