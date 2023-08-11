package bke.iso.engine.input.v2

import com.badlogic.gdx.Gdx

data class MouseBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

data class KeyBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

class KeyboardMouseSource : InputSource() {

    override fun bind(action: String, binding: Binding) {
        if (binding is MouseBinding) {
            buttonBindings[action] = binding
        } else if (binding is KeyBinding) {
            buttonBindings[action] = binding
        }
    }

    override fun checkButtonDown(binding: Binding) =
        when (binding) {
            is MouseBinding -> Gdx.input.isButtonPressed(binding.code)
            is KeyBinding -> Gdx.input.isKeyPressed(binding.code)
            else -> false
        }

    override fun poll(action: String) =
        buttonBindings.poll(action)
}
