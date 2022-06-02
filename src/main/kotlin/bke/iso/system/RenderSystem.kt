package bke.iso.system

import bke.iso.asset.AssetService
import bke.iso.world.*
import bke.iso.world.entity.Entity
import bke.iso.world.entity.PositionComponent
import bke.iso.world.entity.TextureComponent
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
        drawCircle(world.getEntityScreenPos(Vector3(0f, 0f, 0f), offset), 20f, Color.RED)
    }

    private fun drawTile(tile: Tile, location: Location) {
        val texture = assetService.getAsset(tile.texture, Texture::class) ?: return
        val pos = world.locationToScreenPos(location, offset)
        batch.draw(texture, pos.x, pos.y)
    }

    private fun drawEntity(entity: Entity) {
        val positionComponent = entity.getComponent<PositionComponent>() ?: return
        val texture = entity.getComponent<TextureComponent>()
            ?.let { assetService.getAsset<Texture>(it.name) }
            ?: return

        val screenPos =
            world.getEntityScreenPos(Vector3(positionComponent.x, positionComponent.y, 0f), offset)
        batch.draw(texture, screenPos.x, screenPos.y)

        // draw debug bounding square
        batch.end()
        // top
        drawLine(
            world.getEntityScreenPos(Vector3(positionComponent.x - 0.5f, positionComponent.y - 0.5f, 0f), offset),
            world.getEntityScreenPos(Vector3(positionComponent.x + 0.5f, positionComponent.y - 0.5f, 0f), offset),
            1f,
            Color.GREEN
        )
        // bottom
        drawLine(
            world.getEntityScreenPos(Vector3(positionComponent.x - 0.5f, positionComponent.y + 0.5f, 0f), offset),
            world.getEntityScreenPos(Vector3(positionComponent.x + 0.5f, positionComponent.y + 0.5f, 0f), offset),
            1f,
            Color.GREEN
        )
        // left
        drawLine(
            world.getEntityScreenPos(Vector3(positionComponent.x - 0.5f, positionComponent.y + 0.5f, 0f), offset),
            world.getEntityScreenPos(Vector3(positionComponent.x - 0.5f, positionComponent.y - 0.5f, 0f), offset),
            1f,
            Color.GREEN
        )
        // right
        drawLine(
            world.getEntityScreenPos(Vector3(positionComponent.x + 0.5f, positionComponent.y + 0.5f, 0f), offset),
            world.getEntityScreenPos(Vector3(positionComponent.x + 0.5f, positionComponent.y - 0.5f, 0f), offset),
            1f,
            Color.GREEN
        )
        batch.begin()
    }

    private fun drawLine(start: Vector2, end: Vector2, width: Float, color: Color) {
        Gdx.gl.glLineWidth(width)
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
