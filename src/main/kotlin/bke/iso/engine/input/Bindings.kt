package bke.iso.engine.input

open class Binding

abstract class ButtonBinding : Binding() {
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

abstract class AxisBinding : Binding() {
    abstract val code: Int
    abstract val invert: Boolean
}

class Bindings {
    private val bindingByAction = mutableMapOf<String, Binding>()
    private val previousFrame = mutableMapOf<Int, Boolean>()
    private val currentFrame = mutableMapOf<Int, Boolean>()

    var isButtonDown: (ButtonBinding) -> Boolean = { false }
    var getAxis: (AxisBinding) -> Float = { 0f }

    operator fun get(action: String): Binding? =
        bindingByAction[action]

    operator fun set(action: String, binding: Binding) {
        bindingByAction[action] = binding
    }

    fun update() {
        previousFrame.clear()
        previousFrame.putAll(currentFrame)
        currentFrame.clear()
        for ((_, binding) in bindingByAction) {
            if (binding is CompositeBinding<*>) {
                currentFrame[binding.negativeBinding.code] = isButtonDown(binding.negativeBinding)
                currentFrame[binding.positiveBinding.code] = isButtonDown(binding.positiveBinding)
            } else if (binding is ButtonBinding) {
                currentFrame[binding.code] = isButtonDown(binding)
            }
        }
    }

    fun poll(action: String): Float =
        when (val binding = bindingByAction[action]) {
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
