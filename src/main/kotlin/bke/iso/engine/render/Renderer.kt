package bke.iso.engine.render

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.toScreen
import bke.iso.engine.math.toVector2
import bke.iso.engine.math.toWorld
import bke.iso.engine.physics.getCollisionData
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScalingViewport

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

    private val batch = PolygonSpriteBatch()

    /**
     * All game objects are rendered to this buffer
     */
    private val frameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, VIRTUAL_WIDTH.toInt(), VIRTUAL_HEIGHT.toInt(), false)

    /**
     * Used only for rendering the frame buffer
     */
    private val fboCamera = OrthographicCamera()

    /**
     * Only used for game-logic, i.e. following the player
     */
    private val camera = OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT)

    /**
     * Used to scale the rendered frame buffer to the screen
     */
    private val viewport = ScalingViewport(Scaling.fill, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, fboCamera)

    private val shapeDrawer = DebugShapeDrawer(batch)
    val debugRenderer = DebugRenderer(shapeDrawer)
    private var debugEnabled = false

    init {
        // enables somewhat pixel-perfect rendering!
        frameBuffer.colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        // ensures that everything rendered on the frame buffer is in the correct position
        fboCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT)
    }

    fun setCameraPos(worldPos: Vector3) {
        val pos = toScreen(worldPos)
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun toggleDebug() {
        debugEnabled = debugEnabled.not()
    }

    fun getCursorPos(): Vector3 {
        val screenPos = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        return toWorld(screenPos.toVector2())
    }

    fun setCursor(textureName: String) {
        val texture = game.assets.get<Texture>(textureName)
        val xHotspot = texture.width / 2
        val yHotspot = texture.height / 2
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot))
        pixmap.dispose()
    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        fboCamera.update()
    }

    fun render() {
        camera.update()

        frameBuffer.begin()
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
        // for proper rendering, objects with nothing behind them must be drawn first
        drawData.filter { it.objectsBehind.isEmpty() }.forEach(::draw)
        drawData.forEach(::draw)
        batch.end()
        frameBuffer.end()

        ScreenUtils.clear(0f, 0f, 0f, 1f)
        viewport.apply()
        batch.projectionMatrix = fboCamera.combined
        batch.begin()
        batch.draw(frameBuffer.colorBufferTexture, 0f, 0f, viewport.worldWidth, viewport.worldHeight, 0f, 0f, 1f, 1f)
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

        // TODO: is this still needed?
//        if (obj is Tile) {
//            max.add(1f, 1f, 0f)
//        }

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
        // TODO: this fixes an odd rendering bug - can we somehow combine this into another condition for simplicity?
        if (a.max.x <= b.min.x) {
            return false
        }

        if (getDepth(a) < getDepth(b)) {
            return false
        }

        // TODO: finish adding cases to fix rendering issues on y-axis
        if (a.max.z <= b.min.z) {
            return false
        }

        return true
    }

    private fun getDepth(data: DrawData): Float {
        val dCenter = data.center.x - data.center.y
        return dCenter + data.min.x - data.min.y
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
        val sprite = actor.components[Sprite::class] ?: return
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

fun makePixel(): Texture {
    val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.WHITE)
    pixmap.fill()
    val texture = Texture(pixmap)
    pixmap.dispose()
    return texture
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
