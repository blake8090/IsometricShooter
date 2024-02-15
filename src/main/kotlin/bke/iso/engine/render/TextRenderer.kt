package bke.iso.engine.render

import bke.iso.engine.math.toScreen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool

class TextRenderer {

    private val pool = object : Pool<TextRenderable>() {
        override fun newObject() = TextRenderable()
    }

    private val renderables = Array<TextRenderable>()

    fun add(text: String, font: BitmapFont, worldPos: Vector3) {
        val renderable = pool.obtain()
        renderable.text = text
        renderable.font = font

        val pos = toScreen(worldPos)
        renderable.x = pos.x
        renderable.y = pos.y

        renderables.add(renderable)
    }

    fun draw(batch: PolygonSpriteBatch) {
        batch.begin()
        for (renderable in renderables) {
            val font = checkNotNull(renderable.font) {
                "Expected a non-null BitmapFont"
            }

            font.draw(
                batch,
                renderable.text,
                renderable.x,
                renderable.y
            )
        }
        batch.end()

        pool.freeAll(renderables)
        renderables.clear()
    }
}
