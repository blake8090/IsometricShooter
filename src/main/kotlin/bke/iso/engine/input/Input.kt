package bke.iso.engine.input

import bke.iso.engine.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers

class Input(events: Game.Events) {

    private val inputMultiplexer = InputMultiplexer()
    private val inputState = InputState(events)

    val keyMouse: KeyMouseInput = KeyMouseInput()
    val controller: ControllerInput = ControllerInput(inputState)

    fun start() {
        Gdx.input.inputProcessor = inputMultiplexer
        addInputProcessor(inputState.keyMouseHandler)
        Controllers.addListener(inputState.controllerHandler)
        inputState.start()
    }

    fun update() {
        keyMouse.update()
        controller.update()
    }

    fun isUsingController(): Boolean =
        inputState.usingController

    fun poll(action: String): Float {
        return if (inputState.usingController) {
            controller.poll(action)
        } else {
            keyMouse.poll(action)
        }
    }

    inline fun onAction(actionName: String, func: (Float) -> Unit) {
        val axis = poll(actionName)
        if (axis != 0f) {
            func.invoke(axis)
        }
    }

    fun addInputProcessor(processor: InputProcessor) {
        inputMultiplexer.addProcessor(0, processor)
    }

    fun removeInputProcessor(processor: InputProcessor) {
        inputMultiplexer.removeProcessor(processor)
    }

    fun addControllerListener(listener: ControllerListener) {
        Controllers.addListener(listener)
    }

    fun removeControllerListener(listener: ControllerListener) {
        Controllers.removeListener(listener)
    }
}
