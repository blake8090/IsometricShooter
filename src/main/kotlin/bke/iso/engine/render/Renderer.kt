package bke.iso.engine.render

import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.pointer.MousePointer
import bke.iso.engine.render.pointer.Pointer
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
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport

const val VIRTUAL_WIDTH = 960f
const val VIRTUAL_HEIGHT = 540f

class Renderer(
    world: World,
    private val assets: Assets,
    events: Game.Events
) {

    private val batch = PolygonSpriteBatch()
    private val gameObjectRenderer = GameObjectRenderer(this, assets, world, events)
    private var pointer: Pointer = MousePointer()

    val debug: DebugRenderer = DebugRenderer()
    val bgShapes: ShapeArray = ShapeArray()
    val fgShapes: ShapeArray = ShapeArray()
    private val shapeRenderer = ShapeRenderer(batch)

    private val bgColor = Color.GRAY

    private val renderTexts = mutableSetOf<RenderText>()

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

    fun setOcclusionTarget(actor: Actor?) {
        gameObjectRenderer.occlusionTarget = actor
    }

    fun update(deltaTime: Float) {
        pointer.update(deltaTime)
    }

    fun getPointerPos(): Vector2 {
        val screenPos = camera.unproject(Vector3(pointer.pos, 0f))
        return Vector2(screenPos.x, screenPos.y)
    }

    fun setPointer(newPointer: Pointer) {
        pointer.hide()
        newPointer.create()
        newPointer.show()
        pointer = newPointer
    }

    fun drawPointer() {
        batch.projectionMatrix = camera.combined
        pointer.draw(batch, getPointerPos())
    }

    fun draw() {
        camera.update()

        drawToFbo()

        Gdx.gl.glClearColor(bgColor.r, bgColor.b, bgColor.g, bgColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        // reset blend function to default
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        drawShapes(bgShapes)
        drawFbo()
        drawShapes(fgShapes)

        batch.projectionMatrix = camera.combined
        shapeRenderer.begin()
        debug.draw(shapeRenderer)
        shapeRenderer.end()

        batch.begin()
        for (renderText in renderTexts) {
            val font = checkNotNull(renderText.font) {
                "Expected a non-null BitmapFont"
            }

            font.draw(
                batch,
                renderText.text,
                renderText.x,
                renderText.y,
            )
        }
        batch.end()

        bgShapes.clear()
        fgShapes.clear()
        renderTexts.clear()
    }

    private fun drawToFbo() {
        batch.projectionMatrix = camera.combined

        fbo.begin()
        batch.begin()

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        // keep FBO's transparent pixels from mixing into other pixels
        batch.setBlendFunctionSeparate(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA,
            GL20.GL_ONE,
            GL20.GL_ONE_MINUS_SRC_ALPHA,
        )

        gameObjectRenderer.draw(batch)

        batch.end()
        fbo.end()
        fboViewport.apply()
    }

    private fun drawShapes(shapes: ShapeArray) {
        batch.projectionMatrix = camera.combined
        shapeRenderer.begin()
        for (shape in shapes) {
            shapeRenderer.drawShape(shape)
        }
        shapeRenderer.end()
    }

    private fun drawFbo() {
        batch.projectionMatrix = fboViewport.camera.combined
        batch.begin()
        batch.draw(fbo.colorBufferTexture, 0f, 0f, fboViewport.worldWidth, fboViewport.worldHeight, 0f, 0f, 1f, 1f)
        batch.end()
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
        val renderText = Pools.obtain(RenderText::class.java)
        renderText.text = text
        renderText.font = font

        val pos = toScreen(worldPos)
        renderText.x = pos.x
        renderText.y = pos.y
        renderTexts.add(renderText)
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
