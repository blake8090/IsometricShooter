package bke.iso.engine.input

class ControllerInput(private val inputState: InputState) {

    private val bindings = Bindings()

    init {
        bindings.isButtonDown = ::checkButtonDown
        bindings.getAxis = ::getAxis
    }

    fun update() {
        bindings.update()
    }

    fun bind(vararg bindings: Pair<String, Binding>) {
        for ((action, binding) in bindings) {
            if (binding is ControllerBinding || binding is ControllerAxisBinding) {
                this.bindings[action] = binding
            }
        }
    }

    fun bind(action: String, negative: ControllerBinding, positive: ControllerBinding) {
        bindings[action] = CompositeBinding(negative, positive)
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
