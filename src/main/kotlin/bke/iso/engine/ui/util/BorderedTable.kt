package bke.iso.engine.ui.util

import bke.iso.engine.render.makePixelTexture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Table

// TODO: load border color from skin
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