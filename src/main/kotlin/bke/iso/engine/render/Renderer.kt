package bke.iso.engine.render

import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.render.pointer.PointerRenderer
import bke.iso.engine.render.shape.ShapeArray
import bke.iso.engine.render.shape.ShapeRenderer
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import mu.KotlinLogging

const val VIRTUAL_WIDTH = 960f
const val VIRTUAL_HEIGHT = 540f

class Renderer(
    world: World,
    private val assets: Assets,
    events: Game.Events
) {

    private val log = KotlinLogging.logger {}

    private val batch = PolygonSpriteBatch()

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

    val pointer = PointerRenderer(camera, batch)

    val debug: DebugRenderer = DebugRenderer()
    val bgShapes: ShapeArray = ShapeArray()
    val fgShapes: ShapeArray = ShapeArray()
    private val shapeRenderer = ShapeRenderer(batch)

    var bgColor: Color = Color.GRAY

    private val gameObjectRenderer = GameObjectRenderer(assets, world, events, debug)
    private val textRenderer = TextRenderer()

    init {
        // enables somewhat pixel-perfect rendering!
        fbo.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }

    fun dispose() {
        batch.dispose()
        fbo.dispose()
        shapeRenderer.dispose()
        log.info { "Disposed Renderer" }
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

    fun setOcclusionTarget(actor: Actor?) {
        gameObjectRenderer.occlusionTarget = actor
    }

    fun draw() {
        camera.update()
        batch.projectionMatrix = camera.combined

        fboBegin()
        gameObjectRenderer.draw(batch)
        fboEnd()

        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.update()
        shapeRenderer.draw(bgShapes)
        drawFbo()
        shapeRenderer.draw(fgShapes)
        debug.draw(shapeRenderer)

        textRenderer.draw(batch)

        bgShapes.clear()
        fgShapes.clear()
    }

    private fun fboBegin() {
        fbo.begin()
        batch.begin()

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // keep FBO's transparent pixels from mixing into other pixels
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )
    }

    private fun fboEnd() {
        batch.end()
        fbo.end()
        fboViewport.apply()

        // reset blend function to default
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )
    }

    private fun drawFbo() {
        batch.projectionMatrix = fboViewport.camera.combined
        fboViewport.camera.up
        batch.begin()
        batch.draw(fbo.colorBufferTexture, 0f, 0f, fboViewport.worldWidth, fboViewport.worldHeight, 0f, 0f, 1f, 1f)
        batch.end()
        batch.projectionMatrix = camera.combined
    }

    fun drawTexture(
        name: String,
        pos: Vector2,
        offset: Vector2,
        scale: Float = 1f,
        alpha: Float = 1f,
        rotation: Float = 0f
    ) {
        val texture = assets.get<Texture>(name)
        val screenPos = Vector2(pos).sub(offset)
        val color = Color(batch.color.r, batch.color.g, batch.color.b, alpha)
        batch.withColor(color) {
            batch.draw(
                /* region = */ TextureRegion(texture),
                /* x = */ screenPos.x,
                /* y = */ screenPos.y,
                /* originX = */ texture.width / 2f,
                /* originY = */ texture.height / 2f,
                /* width = */ texture.width.toFloat(),
                /* height = */ texture.height.toFloat(),
                /* scaleX = */ scale,
                /* scaleY = */ scale,
                /* rotation = */ rotation
            )
        }
    }

    fun drawText(text: String, font: BitmapFont, worldPos: Vector3) {
        textRenderer.add(text, font, worldPos)
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
