package bke.iso.engine

import bke.iso.app.service.Service
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys

@Service
class Input {
    private val bindings = mutableMapOf<Int, String>()
    private val axisValueByAction = mutableMapOf<String, Float>()

    fun bind(keycode: Int, actionName: String) {
        bindings[keycode] = actionName
        log.debug("Bound key '${Keys.toString(keycode)}' to action '$actionName'")
    }

    fun update() {
        axisValueByAction.clear()
        bindings.forEach { (keycode, actionName) ->
            if (Gdx.input.isKeyPressed(keycode)) {
                axisValueByAction[actionName] = 1f
            }
        }
    }

    fun onAction(actionName: String, handler: (Float) -> Unit) =
        axisValueByAction[actionName]
            ?.let(handler::invoke)
}
