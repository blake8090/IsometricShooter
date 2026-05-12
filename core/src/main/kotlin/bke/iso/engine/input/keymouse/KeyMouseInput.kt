package bke.iso.engine.input.keymouse

import bke.iso.engine.input.Bindings
import bke.iso.engine.input.ButtonBinding
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.CompositeButtonBinding
import com.badlogic.gdx.Gdx

class KeyMouseInput {

    private val bindings = Bindings()

    init {
        bindings.isButtonDown = ::checkButtonDown
    }

    fun update() {
        bindings.update()
    }

    fun bindKey(action: String, key: Int, state: ButtonState) {
        bindings[action] = KeyBinding(key, state)
    }

    fun bindCompositeKey(
        action: String,
        negativeKey: Int,
        negativeState: ButtonState,
        positiveKey: Int,
        positiveState: ButtonState
    ) {
        bindings[action] = CompositeButtonBinding(
            KeyBinding(negativeKey, negativeState),
            KeyBinding(positiveKey, positiveState)
        )
    }

    fun bindMouse(action: String, button: Int, state: ButtonState) {
        bindings[action] = MouseBinding(button, state)
    }

    fun poll(action: String): Float =
        bindings.poll(action)

    private fun checkButtonDown(binding: ButtonBinding) =
        when (binding) {
            is KeyBinding -> Gdx.input.isKeyPressed(binding.code)
            is MouseBinding -> Gdx.input.isButtonPressed(binding.code)
            else -> false
        }
}
