package bke.iso.engine.input.keymouse

import bke.iso.engine.input.Binding
import bke.iso.engine.input.Bindings
import bke.iso.engine.input.ButtonBinding
import bke.iso.engine.input.CompositeBinding
import com.badlogic.gdx.Gdx

class KeyMouseInput {

    private val bindings = Bindings()

    init {
        bindings.isButtonDown = ::checkButtonDown
    }

    fun update() {
        bindings.update()
    }

    fun bind(vararg bindings: Pair<String, Binding>) {
        for ((action, binding) in bindings) {
            if (binding is KeyBinding || binding is MouseBinding) {
                this.bindings[action] = binding
            }
        }
    }

    fun bind(action: String, negative: KeyBinding, positive: KeyBinding) {
        bindings[action] = CompositeBinding(negative, positive)
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
