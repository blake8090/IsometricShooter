package bke.iso.engine.input

import com.badlogic.gdx.Gdx

data class KeyBinding(
    val key: Int,
    val state: InputState,
    val negateAxis: Boolean = false
) : Binding()

class KeyInput {
    private val keysPreviousFrame = mutableMapOf<Int, Boolean>()
    private val keysCurrentFrame = mutableMapOf<Int, Boolean>()

    fun update(bindings: List<KeyBinding>) {
        keysPreviousFrame.clear()
        keysPreviousFrame.putAll(keysCurrentFrame)
        keysCurrentFrame.clear()

        bindings.map(KeyBinding::key)
            .associateWith(Gdx.input::isKeyPressed)
            .forEach(keysCurrentFrame::put)
    }

    fun poll(binding: KeyBinding): Float =
        if (binding.state !in getKeyStates(binding.key)) {
            0f
        } else if (binding.negateAxis) {
            -1f
        } else {
            1f
        }

    private fun getKeyStates(key: Int): List<InputState> {
        val previousFrame = keysPreviousFrame[key] ?: false
        val currentFrame = keysCurrentFrame[key] ?: false
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