package bke.iso.v2.engine.render

import bke.iso.engine.math.toScreen
import bke.iso.engine.render.Sprite
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.GameObject
import bke.iso.v2.engine.world.Tile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3

class Renderer(private val game: Game) : Module(game) {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

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
    }

    private fun toDrawData(obj: GameObject): DrawData {
//        val data = findCollisionData(obj)
//        val min = data?.box?.min ?: obj.pos
//        val max = data?.box?.max ?: obj.pos
        val min = obj.pos
        val max = obj.pos

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
            is Actor-> draw(gameObject)
            is Tile -> draw(gameObject)
        }
    }

    private fun draw(actor: Actor) {
        val sprite = actor.components[Sprite::class] ?: return

        val texture = game.assets.getTexture(sprite.texture)
            ?: throw IllegalStateException("texture ${sprite.texture} not found")

        val screenPos = toScreen(actor.x, actor.y, actor.z)
            .sub(sprite.offsetX, sprite.offsetY)
        batch.draw(texture, screenPos.x, screenPos.y)
    }

    private fun draw(tile: Tile) {
        val texture = game.assets.getTexture(tile.texture)
            ?: throw IllegalStateException("texture ${tile.texture} not found")

        val screenPos = toScreen(tile.x, tile.y, tile.z)
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
