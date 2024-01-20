package bke.iso.engine.render

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Pool.Poolable

data class RenderText(
    var text: String? = null,
    var font: BitmapFont? = null,
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) : Poolable {

    override fun reset() {
        text = null
        font = null
        x = 0f
        y = 0f
        z = 0f
    }
}
