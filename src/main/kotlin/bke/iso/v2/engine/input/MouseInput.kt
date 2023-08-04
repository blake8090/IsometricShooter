package bke.iso.v2.engine.input

import com.badlogic.gdx.Gdx

data class MouseBinding(
    val button: Int,
    val state: InputState,
    val negateAxis: Boolean = false
) : Binding()

class MouseInput {
    private val buttonsPreviousFrame = mutableMapOf<Int, Boolean>()
    private val buttonsCurrentFrame = mutableMapOf<Int, Boolean>()

    fun update(bindings: List<MouseBinding>) {
        buttonsPreviousFrame.clear()
        buttonsPreviousFrame.putAll(buttonsCurrentFrame)
        buttonsCurrentFrame.clear()

        bindings.map(MouseBinding::button)
            .associateWith(Gdx.input::isButtonPressed)
            .forEach(buttonsCurrentFrame::put)
    }

    fun poll(binding: MouseBinding) =
        if (binding.state !in getButtonStates(binding.button)) {
            0f
        } else if (binding.negateAxis) {
            -1f
        } else {
            1f
        }

    private fun getButtonStates(button: Int): List<InputState> {
        val previousFrame = buttonsPreviousFrame[button] ?: false
        val currentFrame = buttonsCurrentFrame[button] ?: false
        val states = mutableListOf<InputState>()

        if (currentFrame) {
            states.add(InputState.DOWN)
        } else {
            states.add(InputState.UP)
        }

        if (currentFrame && !previousFrame) {
            states.add(InputState.PRESSED)
        }

        if (!currentFrame && previousFrame) {
            states.add(InputState.RELEASED)
        }

        return states
    }
}
