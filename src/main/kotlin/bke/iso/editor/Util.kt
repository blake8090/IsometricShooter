package bke.iso.editor

import bke.iso.engine.render.makePixelTexture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Table

class BorderedTable(borderColor: Color) : Table() {

    private var pixel = makePixelTexture(borderColor)

    var borderSize: Float = 1f
    var borderLeft: Boolean = true
    var borderRight: Boolean = true
    var borderTop: Boolean = true
    var borderBottom: Boolean = true

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        // TODO: similar to figma, add inside, outside and center options for borders

        if (borderTop) {
            batch.draw(pixel, x, y + height - borderSize, width, borderSize)
        }

        if (borderBottom) {
            batch.draw(pixel, x, y, width, borderSize)
        }

        if (borderLeft) {
            batch.draw(pixel, x, y, borderSize, height)
        }

        if (borderRight) {
            batch.draw(pixel, x + width - borderSize, y, borderSize, height)
        }
    }
}

/**
 * Utility to create a [Color] from RGBA values provided in a 0 to 255 range
 */
fun color(r: Int, g: Int, b: Int, a: Int = 255): Color =
    Color(
        (r / 255f).coerceIn(0f, 1f),
        (g / 255f).coerceIn(0f, 1f),
        (b / 255f).coerceIn(0f, 1f),
        (a / 255f).coerceIn(0f, 1f)
    )
