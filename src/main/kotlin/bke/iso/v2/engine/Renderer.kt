package bke.iso.v2.engine

import bke.iso.engine.math.toScreen
import bke.iso.engine.render.Sprite
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.Tile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Renderer(private val game: Game) : Module(game) {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        for (gameObject in game.world.objects) {
            when (gameObject) {
                is Actor -> draw(gameObject)
                is Tile -> draw(gameObject)
            }
        }
        batch.end()
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
