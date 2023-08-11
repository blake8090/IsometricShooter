package bke.iso.engine.input.source

import bke.iso.engine.input.ButtonBinding
import bke.iso.engine.input.ButtonState

class ButtonBindings {

    private val bindings = mutableMapOf<String, ButtonBinding>()
    private val previousFrame = mutableMapOf<ButtonBinding, Boolean>()
    private val currentFrame = mutableMapOf<ButtonBinding, Boolean>()

    var isButtonDown: (ButtonBinding) -> Boolean = { false }

    operator fun get(action: String): ButtonBinding? =
        bindings[action]

    operator fun set(action: String, binding: ButtonBinding) {
        bindings[action] = binding
    }

    operator fun contains(action: String) =
        bindings.contains(action)

    fun remove(action: String) =
        bindings.remove(action)

    fun update() {
        previousFrame.clear()
        previousFrame.putAll(currentFrame)
        currentFrame.clear()

        bindings
            .values
            .associateWith(isButtonDown)
            .forEach(currentFrame::put)
    }

    fun poll(action: String): Float {
        val binding = bindings[action] ?: return 0f
        return if (binding.state in getButtonStates(binding)) {
            1f
        } else {
            0f
        }
    }

    private fun getButtonStates(binding: ButtonBinding): Set<ButtonState> {
        val previous = previousFrame[binding] ?: false
        val current = currentFrame[binding] ?: false

        val states = mutableSetOf<ButtonState>()
        if (current) {
            states.add(ButtonState.DOWN)
        } else {
            states.add(ButtonState.UP)
        }

        if (current && !previous) {
            states.add(ButtonState.PRESSED)
        } else if (!current && previous) {
            states.add(ButtonState.RELEASED)
        }

        return states
    }
}
