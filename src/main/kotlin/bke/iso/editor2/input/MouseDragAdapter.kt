package bke.iso.editor2.input

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector2
import io.github.oshai.kotlinlogging.KotlinLogging

class MouseDragAdapter(private val triggerButton: Int) : InputAdapter() {

    private val log = KotlinLogging.logger {}

    private var tracking = false
    private val lastPos = Vector2()
    private val delta = Vector2()

    fun getDelta(): Vector2 {
        val copy = delta.cpy()
        delta.setZero()
        return copy
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == triggerButton) {
            log.trace { "start tracking" }
            lastPos.set(screenX.toFloat(), screenY.toFloat())
            tracking = true
        }
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == triggerButton) {
            log.trace { "stop tracking" }
            delta.setZero()
            tracking = false
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (tracking) {
            delta.set(
                screenX - lastPos.x,
                screenY - lastPos.y
            )
            lastPos.set(screenX.toFloat(), screenY.toFloat())
            log.trace { "mouse drag: $delta pos: ($screenX,$screenY)" }
        }
        return false
    }
}
