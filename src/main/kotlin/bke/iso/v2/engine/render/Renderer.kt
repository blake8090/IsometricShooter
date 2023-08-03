package bke.iso.v2.engine.render

import bke.iso.engine.math.toScreen
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.debug.DebugShapeDrawer
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import bke.iso.v2.engine.physics.getCollisionData
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.GameObject
import bke.iso.v2.engine.world.Tile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector3

class Renderer(private val game: Game) : Module(game) {

    private val batch = PolygonSpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)

    private val shapeDrawer = DebugShapeDrawer(batch)
    val debugRenderer = DebugRenderer()

    fun setCameraPos(worldPos: Vector3) {
        val pos = toScreen(worldPos)
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
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

        debugRenderer.render(shapeDrawer)
        // debug data still accumulates even when not in debug mode!
        debugRenderer.clear()
    }

    private fun toDrawData(obj: GameObject): DrawData {
        val data = obj.getCollisionData()
        val min = data?.box?.min ?: obj.pos
        val max = data?.box?.max ?: obj.pos

        // TODO: is this still needed?
//        if (obj is Tile) {
//            max.add(1f, 1f, 0f)
//        }

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
        //addActorDebugShapes(actor)
    }

    private fun addActorDebugShapes(actor: Actor) {
        debugRenderer.addPoint(actor.pos, 2f, Color.RED)

        actor.getCollisionData()?.let { data ->
            debugRenderer.addBox(data.box, Color.GREEN)
        }

//        if (entity.z != 0f) {
//            val start = Vector3(entity.x, entity.y, 0f)
//            val end = entity.pos
//            debugRenderService.addPoint(start, 2f, Color.RED)
//            debugRenderService.addLine(start, end, 1f, Color.PURPLE)
//        }
    }

    private fun draw(tile: Tile) {
        drawSprite(tile.sprite, tile.pos)
    }

    private fun drawSprite(sprite: Sprite, worldPos: Vector3) {
        val texture = game.assets.get<Texture>(sprite.texture)
        val screenPos = toScreen(worldPos)
            .sub(sprite.offsetX, sprite.offsetY)
        batch.draw(texture, screenPos.x, screenPos.y)
    }
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
