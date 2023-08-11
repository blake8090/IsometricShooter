package bke.iso.engine.input

sealed class Binding

sealed class ButtonBinding : Binding() {
    abstract val code: Int
    abstract val state: ButtonState
}

enum class ButtonState {
    UP,
    DOWN,
    PRESSED,
    RELEASED
}

data class CompositeBinding<T : ButtonBinding>(
    val negativeBinding: T,
    val positiveBinding: T
) : Binding()

sealed class AxisBinding : Binding() {
    abstract val code: Int
}

data class MouseBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

data class KeyBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()


data class ControllerBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

data class ControllerAxisBinding(override val code: Int) : AxisBinding()

class Bindings {
    private val bindings = mutableMapOf<String, Binding>()
    private val previousFrame = mutableMapOf<Int, Boolean>()
    private val currentFrame = mutableMapOf<Int, Boolean>()

    var isButtonDown: (ButtonBinding) -> Boolean = { false }
    var getAxis: (AxisBinding) -> Float = { 0f }

    operator fun get(action: String): Binding? =
        bindings[action]

    operator fun set(action: String, binding: Binding) {
        bindings[action] = binding
    }

    fun update() {
        previousFrame.clear()
        previousFrame.putAll(currentFrame)
        currentFrame.clear()
        for ((_, binding) in bindings) {
            if (binding is CompositeBinding<*>) {
                currentFrame[binding.negativeBinding.code] = isButtonDown(binding.negativeBinding)
                currentFrame[binding.positiveBinding.code] = isButtonDown(binding.positiveBinding)
            } else if (binding is ButtonBinding) {
                currentFrame[binding.code] = isButtonDown(binding)
            }
        }
    }

    fun poll(action: String): Float =
        when (val binding = bindings[action]) {
            is ButtonBinding -> pollButtonBinding(binding)

            is CompositeBinding<*> -> pollCompositeBinding(binding)

            is AxisBinding -> getAxis(binding)

            else -> 0f
        }

    private fun pollButtonBinding(binding: ButtonBinding): Float =
        if (binding.state in getButtonStates(binding)) {
            1f
        } else {
            0f
        }

    private fun pollCompositeBinding(binding: CompositeBinding<*>): Float {
        val negative = pollButtonBinding(binding.negativeBinding)
        val positive = pollButtonBinding(binding.positiveBinding)
        return if (negative != 0f) {
            negative * -1f
        } else {
            positive
        }
    }

    private fun getButtonStates(binding: ButtonBinding): Set<ButtonState> {
        val previous = previousFrame[binding.code] ?: false
        val current = currentFrame[binding.code] ?: false

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
