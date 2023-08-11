package bke.iso.engine.input.v2

import com.badlogic.gdx.controllers.Controller

data class ControllerBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

data class ControllerAxisBinding(override val code: Int) : AxisBinding()

class ControllerSource : InputSource() {

    private val controller: Controller? = null

    private val axisBindings = mutableMapOf<String, AxisBinding>()

    override fun bind(action: String, binding: Binding) {
        if (binding is ControllerBinding) {
            buttonBindings[action] = binding
            axisBindings.remove(action)
        } else if (binding is ControllerAxisBinding) {
            axisBindings[action] = binding
            buttonBindings.remove(action)
        }
    }

    override fun checkButtonDown(binding: Binding): Boolean {
        return if (binding is ControllerBinding) {
            controller
                ?.getButton(binding.code)
                ?: false
        } else {
            false
        }
    }

    override fun poll(action: String): Float {
        return if (buttonBindings.contains(action)) {
            buttonBindings.poll(action)
        } else {
            val binding = axisBindings[action] ?: return 0f
            controller
                ?.getAxis(binding.code)
                ?: 0f
        }
    }
}
