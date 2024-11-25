package bke.iso.editor.scene.camera

import com.badlogic.gdx.InputAdapter

class MouseScrollAdapter : InputAdapter() {

    private var amountX: Float = 0f
    private var amountY: Float = 0f

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        this.amountX = amountX
        this.amountY = amountY
        return super.scrolled(amountX, amountY)
    }

    fun onScroll(action: (Float, Float) -> Unit) {
        if (amountX == 0f && amountY == 0f) {
            return
        }

        action.invoke(amountX, amountY)

        amountX = 0f
        amountY = 0f
    }
}
