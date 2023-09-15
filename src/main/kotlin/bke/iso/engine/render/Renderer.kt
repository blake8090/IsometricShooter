package bke.iso.engine.render

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.shape.Shape3dArray
import bke.iso.engine.render.shape.Shape3dDrawer
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScalingViewport
import mu.KotlinLogging

private const val VIRTUAL_WIDTH = 960f
private const val VIRTUAL_HEIGHT = 540f

// TODO: inner class?
data class DrawActorEvent(
    val actor: Actor,
    val batch: PolygonSpriteBatch
) : Event

class Renderer(
    private val world: World,
    private val assets: Assets,
    private val events: Game.Events
) {

    private val log = KotlinLogging.logger {}

    private val batch = PolygonSpriteBatch()
    private val objectSorter = ObjectSorter()
    private var customCursor: CustomCursor? = null

    val debug: DebugRenderer = DebugRenderer()

    val shapes: Shape3dArray = Shape3dArray()
    private val shapeDrawer = Shape3dDrawer(batch)

    // TODO: cleanup this fbo code, see LowResGDX on github
    /**
     * Game world is drawn to this FBO. Enables things such as post-processing and pixel-perfect scaling.
     */
    private val fbo = FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH.toInt(), VIRTUAL_HEIGHT.toInt(), false)

    /**
     * Used for scaling the FBO to the main screen
     */
    private val fboViewport = ScalingViewport(Scaling.fill, VIRTUAL_WIDTH, VIRTUAL_HEIGHT)

    /**
     * Only used for game-logic, i.e. following the player
     */
    private val camera = OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT)

    init {
        // enables somewhat pixel-perfect rendering!
        fbo.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }

    fun dispose() {
        batch.dispose()
        fbo.dispose()
    }

    fun resize(width: Int, height: Int) {
        fboViewport.update(width, height, true)
    }

    fun moveCamera(delta: Vector2) {
        camera.position.add(delta.x, delta.y, 0f)
    }

    fun setCameraPos(worldPos: Vector3) {
        camera.position.set(toScreen(worldPos), 0f)
    }

    fun getCursorPos(): Vector2 {
        val pos = customCursor
            ?.getPos()
            ?: Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())

        val screenPos = camera.unproject(Vector3(pos, 0f))
        return Vector2(screenPos.x, screenPos.y)
    }

    fun setCursor(customCursor: CustomCursor) {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None)
        customCursor.create()
        this.customCursor = customCursor
        log.debug { "Set custom cursor: ${customCursor::class.simpleName}" }
    }

    fun resetCursor() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        customCursor = null
        log.debug { "Reset cursor" }
    }

    fun updateCursor(deltaTime: Float) {
        customCursor?.update(deltaTime)
    }

    fun drawCursor() {
        val cursor = customCursor ?: return
        batch.projectionMatrix = camera.combined
        cursor.draw(batch, getCursorPos())
    }

    fun draw() {
        camera.update()
        batch.projectionMatrix = camera.combined

        fbo.begin()
        ScreenUtils.clear(0f, 0f, 255f, 1f)

        // TODO: find a way to draw full size shapes underneath FBO
        shapeDrawer.begin()
        for (shape in shapes) {
            shapeDrawer.drawShape(shape)
        }
        shapeDrawer.end()

        batch.begin()
        objectSorter.forEach(world.getObjects()) {
            when (it) {
                is Actor -> draw(it)
                is Tile -> draw(it)
            }
        }
        batch.end()
        fbo.end()

        ScreenUtils.clear(0f, 0f, 0f, 1f)
        fboViewport.apply()
        batch.projectionMatrix = fboViewport.camera.combined
        batch.begin()
        batch.draw(fbo.colorBufferTexture, 0f, 0f, fboViewport.worldWidth, fboViewport.worldHeight, 0f, 0f, 1f, 1f)
        batch.end()

        // make sure that shapes are drawn respective to world positions
        batch.projectionMatrix = camera.combined
        shapeDrawer.begin()
        debug.draw(shapeDrawer)
        shapeDrawer.end()

        shapes.clear()
    }

    private fun draw(actor: Actor) {
        val sprite = actor.get<Sprite>() ?: return
        drawSprite(sprite, actor.pos)
        debug.add(actor)
        events.fire(DrawActorEvent(actor, batch))
    }

    private fun draw(tile: Tile) {
        drawSprite(tile.sprite, tile.location.toVector3())
        debug.add(tile)
    }

    private fun drawSprite(sprite: Sprite, worldPos: Vector3) {
        val texture = assets.get<Texture>(sprite.texture)
        val screenPos = toScreen(worldPos)
            .sub(sprite.offsetX, sprite.offsetY)

        val width = texture.width * sprite.scale
        val height = texture.height * sprite.scale
        // when scaling textures, make sure texture is still centered on origin point
        if (sprite.scale != 1f) {
            val diffX = texture.width - width
            val diffY = texture.height - height
            screenPos.add(diffX / 2f, diffY / 2f)
        }
        val color = Color(batch.color.r, batch.color.g, batch.color.b, sprite.alpha)
        batch.withColor(color) {
            batch.draw(texture, screenPos.x, screenPos.y, width, height)
        }
    }
}

fun Batch.withColor(color: Color, action: (Batch) -> Unit) {
    val originalColor = Color(this.color)
    this.color = color
    action.invoke(this)
    this.color = originalColor
}

fun makePixelTexture(color: Color = Color.WHITE): Texture {
    val pixmap = makePixel(color)
    val texture = Texture(pixmap)
    pixmap.dispose()
    return texture
}

fun makePixel(color: Color = Color.WHITE): Pixmap {
    val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.drawPixel(0, 0)
    return pixmap
}
