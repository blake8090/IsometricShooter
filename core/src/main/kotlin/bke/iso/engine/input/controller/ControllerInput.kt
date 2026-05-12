package bke.iso.engine.input.controller

import bke.iso.engine.input.AxisBinding
import bke.iso.engine.input.Bindings
import bke.iso.engine.input.ButtonBinding
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.InputState
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton

class ControllerInput(private val inputState: InputState) {

    private val bindings = Bindings()

    init {
        bindings.isButtonDown = ::checkButtonDown
        bindings.getAxis = ::getAxis
    }

    fun update() {
        bindings.update()
    }

    fun bindButton(action: String, button: ControllerButton, state: ButtonState) {
        bindings[action] = ControllerBinding(button.ordinal, state)
    }

    fun bindAxis(action: String, axis: ControllerAxis, invert: Boolean = false) {
        bindings[action] = ControllerAxisBinding(axis.ordinal, invert)
    }

    fun poll(action: String): Float =
        bindings.poll(action)

    private fun checkButtonDown(binding: ButtonBinding) =
        if (binding is ControllerBinding) {
            inputState.findPrimaryController()
                ?.getButton(binding.code)
                ?: false
        } else {
            false
        }

    private fun getAxis(binding: AxisBinding): Float {
        val axis = inputState.findPrimaryController()
            ?.getAxis(binding.code)
            ?: return 0f

        return if (binding.invert) {
            axis * -1f
        } else {
            axis
        }
    }
}
