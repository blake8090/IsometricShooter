package bke.iso.engine.render.text

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Pool

class TextRenderable(
    var font: BitmapFont? = null,
    var text: String = "",
    var color: Color = Color.WHITE,
    var x: Float = 0f,
    var y: Float = 0f,
) : Pool.Poolable {

    override fun reset() {
        font = null
        text = ""
        color = Color.WHITE
        x = 0f
        y = 0f
    }
}
