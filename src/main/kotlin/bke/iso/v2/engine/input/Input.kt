package bke.iso.v2.engine.input

import bke.iso.engine.input.Binding
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.KeyInput
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.input.MouseInput
import bke.iso.engine.log
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2

class Input(game: Game) : Module(game) {

    private val keyInput = KeyInput()
    private val mouseInput = MouseInput()
    private val bindings = mutableMapOf<String, Binding>()

    fun <T : Binding> bind(actionName: String, binding: T) {
        bindings[actionName] = binding
        log.debug("Bound '{}' to {} '{}'", actionName, binding::class.simpleName, binding)
    }

    override fun update(deltaTime: Float) {
        keyInput.update(bindings.values.filterIsInstance<KeyBinding>())
        mouseInput.update(bindings.values.filterIsInstance<MouseBinding>())
    }

    fun poll(actionName: String): Float =
        when (val binding = bindings[actionName] ?: 0f) {
            is KeyBinding -> keyInput.poll(binding)
            is MouseBinding -> mouseInput.poll(binding)
            else -> 0f
        }

    fun poll(vararg actionNames: String): Float =
        actionNames.map(this::poll)
            .find { it != 0f }
            ?: 0f

    fun onAction(actionName: String, func: (Float) -> Unit) {
        val axis = poll(actionName)
        if (axis != 0f) {
            func.invoke(axis)
        }
    }

    fun getMousePos() =
        Vector2(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )
}