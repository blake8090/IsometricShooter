package bke.iso.render

import bke.iso.asset.AssetService
import bke.iso.di.SingletonImpl
import bke.iso.world.Location
import bke.iso.world.Tile
import bke.iso.world.World
import bke.iso.world.entity.Entity
import bke.iso.world.entity.PositionComponent
import bke.iso.world.entity.TextureComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

@SingletonImpl(Renderer::class)
class LibGdxRenderer(
    private val assetService: AssetService,
    private val world: World
) : Renderer {
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera(1280f, 720f)

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined
        drawMap()
    }

    override fun setCameraPos(x: Float, y: Float) {
        camera.position.x = x
        camera.position.y = y
    }

    private fun drawMap() {
        batch.begin()
        world.forEachLocation { location, tile, ids ->
            tile?.let { drawTile(it, location) }
            ids.forEach(this::drawEntity)
        }
        batch.end()
    }

    private fun drawTile(tile: Tile, location: Location) {
        val texture = assetService.getAsset(tile.texture, Texture::class) ?: return
        val pos = world.unitConverter.tileToScreen(location)
        batch.draw(texture, pos.x, pos.y)
    }

    private fun drawEntity(entity: Entity) {
        val positionComponent = entity.findComponent<PositionComponent>() ?: return
        val texture = entity.findComponent<TextureComponent>()
            ?.let { assetService.getAsset<Texture>(it.name) }
            ?: return

        val screenPos = world.unitConverter.worldToScreen(positionComponent)
        // TODO: add a sprite origin component to entities to abstract this offset
        batch.draw(texture, screenPos.x - 32f, screenPos.y)

        // draw debug bounding square
        batch.end()
        drawWorldRectangle(
            positionComponent.x,
            positionComponent.y,
            1f,
            1f
        )

        world.unitConverter.worldToScreen(positionComponent).apply {
            drawCircle(x, y, 1f, Color.RED)
        }

        batch.begin()
    }

    private fun drawWorldRectangle(x: Float, y: Float, width: Float, height: Float) {
        Gdx.gl.glLineWidth(width)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.GREEN

        world.unitConverter.apply {
            // top
            shapeRenderer.line(
                worldToScreen(x, y),
                worldToScreen(x + width, y),
            )
            // bottom
            shapeRenderer.line(
                worldToScreen(x, y + height),
                worldToScreen(x + width, y + height),
            )
            // left
            shapeRenderer.line(
                worldToScreen(x, y),
                worldToScreen(x, y + height),
            )
            // right
            shapeRenderer.line(
                worldToScreen(x + width, y),
                worldToScreen(x + width, y + height),
            )
        }

        shapeRenderer.end()

        // resets line width to default
        Gdx.gl.glLineWidth(1f)
    }

    private fun drawCircle(x: Float, y: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.circle(x, y, size)
        shapeRenderer.end()
    }
}
