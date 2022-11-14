package bke.iso.engine

import bke.iso.app.service.Service
import com.badlogic.gdx.Gdx

enum class KeyState {
    UP,
    DOWN,
    PRESSED,
    RELEASED
}

@Service
class Input {
    private val keysPreviousFrame = mutableMapOf<Int, Boolean>()
    private val keysCurrentFrame = mutableMapOf<Int, Boolean>()
    private val keyBindings = mutableMapOf<String, KeyBinding>()

    fun bind(actionName: String, key: Int, state: KeyState, negateAxis: Boolean = false) {
        val keyBinding = KeyBinding(key, state, negateAxis)
        keyBindings[actionName] = keyBinding
        log.debug("Bound '$actionName' to $keyBinding")
    }

    fun update() {
        keysPreviousFrame.clear()
        keysPreviousFrame.putAll(keysCurrentFrame)
        keysCurrentFrame.clear()

        keyBindings.values
            .map(KeyBinding::key)
            .associateWith(Gdx.input::isKeyPressed)
            .forEach(keysCurrentFrame::put)
    }

    fun pollAction(actionName: String): Float {
        val keyBinding = keyBindings[actionName] ?: return 0f
        if (keyBinding.state !in getKeyStates(keyBinding.key)) {
            return 0f
        }

        return if (keyBinding.negateAxis) {
            -1f
        } else {
            1f
        }
    }

    fun pollActions(vararg actionName: String): Float =
        actionName.map(this::pollAction)
            .firstOrNull { axis -> axis != 0f }
            ?: 0f

    private fun getKeyStates(key: Int): List<KeyState> {
        val previousFrame = keysPreviousFrame[key] ?: false
        val currentFrame = keysCurrentFrame[key] ?: false
        val states = mutableListOf<KeyState>()

        if (currentFrame) {
            states.add(KeyState.DOWN)
        } else {
            states.add(KeyState.UP)
        }

        if (currentFrame && !previousFrame) {
            states.add(KeyState.PRESSED)
        }

        if (!currentFrame && previousFrame) {
            states.add(KeyState.RELEASED)
        }

        return states
    }
}

private data class KeyBinding(
    val key: Int,
    val state: KeyState,
    val negateAxis: Boolean = false
)
