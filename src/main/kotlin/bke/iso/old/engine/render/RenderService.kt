package bke.iso.old.engine.render

import bke.iso.old.engine.asset.AssetService
import bke.iso.old.engine.entity.Entity
import bke.iso.old.engine.event.EventService
import bke.iso.old.engine.math.toScreen
import bke.iso.old.engine.math.toVector2
import bke.iso.old.engine.math.toWorld
import bke.iso.old.engine.physics.CollisionService
import bke.iso.old.engine.render.debug.DebugRenderService
import bke.iso.old.engine.render.debug.DebugShapeDrawer
import bke.iso.old.engine.world.Tile
import bke.iso.old.engine.world.WorldObject
import bke.iso.old.engine.world.WorldService
import bke.iso.old.service.SingletonService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class RenderService(
    private val assetService: AssetService,
    private val worldService: WorldService,
    private val collisionService: CollisionService,
    private val eventService: EventService,
    private val debugRenderService: DebugRenderService
) : SingletonService {

    private val batch = PolygonSpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)
    private val shapeDrawer = DebugShapeDrawer(batch)

    private var debugMode = false

    private val shadowSprite = Sprite("shadow", 16f, 16f)

    fun toggleDebugMode() {
        debugMode = !debugMode
    }

    fun setCameraPos(worldPos: Vector3) {
        val pos = toScreen(worldPos)
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun unproject(screenCoords: Vector2): Vector3 {
        val screenPos = camera.unproject(Vector3(screenCoords.x, screenCoords.y, 0f))
        return toWorld(screenPos.toVector2())
    }

    fun setCursor(textureName: String) {
        val texture = assetService.require<Texture>(textureName)
        val xHotspot = texture.width / 2
        val yHotspot = texture.height / 2
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot))
        pixmap.dispose()
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


        shapeDrawer.update()

        batch.begin()
        val drawData = worldService.getAllObjects().map(::toDrawData)
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

        if (debugMode) {
            debugRenderService.render(shapeDrawer)
        }
        // debug data still accumulates even when not in debug mode!
        debugRenderService.clear()
    }

    override fun dispose() {
        batch.dispose()
        shapeDrawer.dispose()
    }

    private fun toDrawData(obj: WorldObject): DrawData {
        val data = findCollisionData(obj)
        val min = data?.box?.min ?: obj.pos
        val max = data?.box?.max ?: obj.pos

        if (obj is Tile) {
            max.add(1f, 1f, 0f)
        }

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

    private fun draw(data: DrawData) {
        if (data.visited) {
            return
        }
        data.visited = true
        data.objectsBehind.forEach(::draw)
        when (val worldObject = data.obj) {
            is Entity -> drawEntity(worldObject)
            is Tile -> drawTile(worldObject)
        }
    }

    private fun getDepth(data: DrawData): Float {
        val dCenter = data.center.x - data.center.y
        return dCenter + data.min.x - data.min.y
    }

    private fun findCollisionData(worldObject: WorldObject) =
        when (worldObject) {
            is Entity -> collisionService.findCollisionData(worldObject)
            else -> null
        }

    private fun drawEntity(entity: Entity) {
        val sprite = entity.get<Sprite>() ?: return
        if (entity.z > 0f && entity.has<DrawShadow>()) {
            drawSprite(shadowSprite, Vector3(entity.x, entity.y, 0f))
        }
        drawSprite(sprite, entity.pos)
        eventService.fire(DrawEntityEvent(entity, batch))
        addEntityDebugData(entity)
    }

    private fun addEntityDebugData(entity: Entity) {
        debugRenderService.addPoint(entity.pos, 2f, Color.RED)

        collisionService.findCollisionData(entity)?.let { data ->
            debugRenderService.addBox(data.box, Color.GREEN)
        }

        if (entity.z != 0f) {
            val start = Vector3(entity.x, entity.y, 0f)
            val end = entity.pos
            debugRenderService.addPoint(start, 2f, Color.RED)
            debugRenderService.addLine(start, end, 1f, Color.PURPLE)
        }
    }

    private fun drawTile(tile: Tile) {
        drawSprite(tile.sprite, tile.pos)
    }

    private fun drawSprite(sprite: Sprite, worldPos: Vector3) {
        val texture = assetService.require<Texture>(sprite.texture)
        val screenPos = toScreen(worldPos)
            .sub(sprite.offsetX, sprite.offsetY)
        batch.draw(texture, screenPos.x, screenPos.y)
    }
}

private data class DrawData(
    val obj: WorldObject,
    val min: Vector3,
    val max: Vector3,
    val center: Vector3
) {
    val objectsBehind = mutableSetOf<DrawData>()
    var visited = false
}