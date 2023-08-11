package bke.iso.engine.input.v2

import bke.iso.engine.Game
import bke.iso.engine.Module

class Input(override val game: Game) : Module() {

    private val keyboardMouseSource = KeyboardMouseSource()
    private val controllerSource = ControllerSource()
    private var controllerConnected = false

    override fun update(deltaTime: Float) {
        keyboardMouseSource.update()
        if (controllerConnected) {
            controllerSource.update()
        }
    }

    fun bind(action: String, binding: Binding) {
        keyboardMouseSource.bind(action, binding)
        controllerSource.bind(action, binding)
    }

    fun poll(action: String): Float {
        return if (controllerConnected) {
            controllerSource.poll(action)
        } else {
            keyboardMouseSource.poll(action)
        }
    }

    fun poll(actionPositive: String, actionNegative: String): Float {
        val positive = poll(actionPositive)
        val negative = poll(actionNegative)
        return if (positive != 0f) {
            positive
        } else if (negative != 0f) {
            negative * -1f
        } else {
            0f
        }
    }

    fun onAction(actionName: String, func: (Float) -> Unit) {
        val axis = poll(actionName)
        if (axis != 0f) {
            func.invoke(axis)
        }
    }
}
