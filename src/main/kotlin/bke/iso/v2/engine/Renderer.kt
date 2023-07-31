package bke.iso.v2.engine

import bke.iso.engine.math.toScreen
import bke.iso.engine.render.Sprite
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
        for (actor in game.world.getActors()) {
            val sprite = actor.components[Sprite::class] ?: continue
            val texture = game.assets.getTexture(sprite.texture)
                ?: throw IllegalStateException("texture ${sprite.texture} not found")

            val screenPos = toScreen(actor.x, actor.y, actor.z)
                .sub(sprite.offsetX, sprite.offsetY)
            batch.draw(texture, screenPos.x, screenPos.y)
        }
        batch.end()
    }
}
