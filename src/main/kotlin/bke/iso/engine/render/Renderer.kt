package bke.iso.engine.render

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.toScreen
import bke.iso.engine.math.toVector2
import bke.iso.engine.physics.getCollisionData
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
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

const val VIRTUAL_WIDTH = 960f
const val VIRTUAL_HEIGHT = 540f

data class Sprite(
    val texture: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Component()

// TODO: inner class?
data class DrawActorEvent(
    val actor: Actor,
    val batch: PolygonSpriteBatch
) : Event()

class Renderer(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    private val batch = PolygonSpriteBatch()

    val debugRenderer = DebugRenderer(DebugShapeDrawer(batch))
    private var debugEnabled = false

    private var customCursor: CustomCursor? = null

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

    override fun dispose() {
        batch.dispose()
        debugRenderer.clear()
        fbo.dispose()
    }

    fun resize(width: Int, height: Int) {
        fboViewport.update(width, height, true)
    }

    fun setCameraPos(worldPos: Vector3) {
        val pos = toScreen(worldPos)
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun toggleDebug() {
        debugEnabled = debugEnabled.not()
    }

    fun getCursorPos(): Vector2 {
        val pos = customCursor
            ?.getPos()
            ?: Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        return camera.unproject(Vector3(pos.x, pos.y, 0f)).toVector2()
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

    fun updateCursor(deltaTime: Float) =
        customCursor?.update(deltaTime)

    fun drawCursor() {
        val cursor = customCursor ?: return
        batch.projectionMatrix = camera.combined
        cursor.draw(batch, getCursorPos())
    }

    fun render() {
        camera.update()
        fbo.begin()
        ScreenUtils.clear(0f, 0f, 255f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()
        val drawData = game.world.objects.map(::toDrawData)
        for ((i, a) in drawData.withIndex()) {
            for ((j, b) in drawData.withIndex()) {
                if (i == j) {
                    continue
                } else if (inFront(a, b)) {
                    a.objectsBehind.add(b)
                } else if (inFront(b, a)) {
                    b.objectsBehind.add(a)
                }
            }
        }
        // TODO: is this really true?
        // for proper rendering, objects with nothing behind them must be drawn first
        drawData.filter { it.objectsBehind.isEmpty() }.forEach(::draw)
        drawData.forEach(::draw)
        batch.end()
        fbo.end()

        ScreenUtils.clear(0f, 0f, 0f, 1f)
        fboViewport.apply()
        batch.projectionMatrix = fboViewport.camera.combined
        batch.begin()
        batch.draw(fbo.colorBufferTexture, 0f, 0f, fboViewport.worldWidth, fboViewport.worldHeight, 0f, 0f, 1f, 1f)
        batch.end()

        if (debugEnabled) {
            // match debug shapes to world positions
            batch.projectionMatrix = camera.combined
            debugRenderer.render()
        }
        // debug data still accumulates even when not in debug mode!
        debugRenderer.clear()
    }

    private fun toDrawData(obj: GameObject): DrawData {
        val data = obj.getCollisionData()
        val pos = when (obj) {
            is Tile -> obj.location.toVector3()
            is Actor -> obj.pos
            else -> error("Unrecognized type for game object $obj")
        }

        val min = data?.box?.min ?: pos
        val max = data?.box?.max ?: pos
        val width = max.x - min.x
        val length = max.y - min.y
        val height = max.z - min.z
        val center = Vector3(
            min.x + (width / 2f),
            min.y + (length / 2f),
            min.z + (height / 2f)
        )

        return DrawData(obj, min, max, center)
    }

    private fun inFront(a: DrawData, b: DrawData): Boolean {
        if (a.max.z <= b.min.z) {
            return false
        }

        if (a.min.y - b.max.y >= 0) {
            return false
        }

        if (a.max.x - b.min.x <= 0) {
            return false
        }

        return true
    }

    private fun draw(data: DrawData) {
        if (data.visited) {
            return
        }
        data.visited = true
        data.objectsBehind.forEach(::draw)
        when (val gameObject = data.obj) {
            is Actor -> draw(gameObject)
            is Tile -> draw(gameObject)
        }
    }

    private fun draw(actor: Actor) {
        val sprite = actor.get<Sprite>() ?: return
        drawSprite(sprite, actor.pos)
        addDebugShapes(actor)
        game.events.fire(DrawActorEvent(actor, batch))
    }

    private fun addDebugShapes(actor: Actor) {
        debugRenderer.addPoint(actor.pos, 2f, Color.RED)

        actor.getCollisionData()?.let { data ->
            debugRenderer.addBox(data.box, 1f, Color.GREEN)
        }

        if (actor.z != 0f) {
            val start = Vector3(actor.x, actor.y, 0f)
            val end = actor.pos
            debugRenderer.addPoint(start, 2f, Color.RED)
            debugRenderer.addLine(start, end, 1f, Color.PURPLE)
        }
    }

    private fun draw(tile: Tile) {
        drawSprite(tile.sprite, tile.location.toVector3())
        debugRenderer.addBox(tile.getCollisionData().box, 1f, Color.WHITE)
    }

    private fun drawSprite(sprite: Sprite, worldPos: Vector3) {
        val texture = game.assets.get<Texture>(sprite.texture)
        val screenPos = toScreen(worldPos)
            .sub(sprite.offsetX, sprite.offsetY)
        batch.draw(texture, screenPos.x, screenPos.y)
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

private data class DrawData(
    val obj: GameObject,
    val min: Vector3,
    val max: Vector3,
    val center: Vector3
) {
    val objectsBehind = mutableSetOf<DrawData>()
    var visited = false
}
