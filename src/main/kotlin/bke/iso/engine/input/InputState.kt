package bke.iso.engine.input

import bke.iso.engine.Game
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.Controllers
import com.studiohartman.jamepad.ControllerButton
import mu.KotlinLogging
import kotlin.math.abs

class InputState(private val events: Game.Events) {

    private val log = KotlinLogging.logger {}

    val keyMouseHandler: InputAdapter = KeyMouseHandler()
    val controllerHandler: ControllerAdapter = ControllerHandler()
    var usingController = false
        private set

    fun start() {
        // when starting the game, a controller should always be used if already connected
        if (findPrimaryController() != null) {
            switchInput(InputSource.CONTROLLER)
        }
    }

    fun findPrimaryController() =
        Controllers.getControllers().find { controller ->
            controller.isConnected && controller.playerIndex == 0
        }

    private fun switchInput(source: InputSource) {
        if (source == InputSource.CONTROLLER && !usingController) {
            log.debug { "Switching input to controller" }
            usingController = true
        } else if (source == InputSource.KEYBOARD_MOUSE && usingController) {
            log.debug { "Switching input to keyboard/mouse" }
            usingController = false
        }
    }

    private inner class KeyMouseHandler : InputAdapter() {

        override fun keyDown(keycode: Int): Boolean {
            switchInput(InputSource.KEYBOARD_MOUSE)
            return false
        }

        override fun keyUp(keycode: Int): Boolean {
            switchInput(InputSource.KEYBOARD_MOUSE)
            return false
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            switchInput(InputSource.KEYBOARD_MOUSE)
            return false
        }
    }

    private inner class ControllerHandler : ControllerAdapter() {

        override fun connected(controller: Controller) {
            log.info { "Controller connected: ${controller.asString()}" }
            // only switch to controller input when the primary controller has been connected
            if (controller.playerIndex == 0) {
                switchInput(InputSource.CONTROLLER)
                events.fire(Input.OnControllerConnect())
            }
        }

        override fun disconnected(controller: Controller) {
            log.info { "Controller disconnected: ${controller.asString()}" }
            // TODO: Fix
            // when the primary controller is disconnected, switch to keyboard/mouse.
            // we don't need to worry about any other controllers
            if (controller.playerIndex == 0) {
                switchInput(InputSource.KEYBOARD_MOUSE)
            }
        }

        override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean {
            val controllerButton = matchButton(buttonIndex)
            log.trace { "Button down: ${controllerButton.name} - ${controller.asString()}" }
            switchInput(InputSource.CONTROLLER)
            return false
        }

        override fun buttonUp(controller: Controller, buttonIndex: Int): Boolean {
            val controllerButton = matchButton(buttonIndex)
            log.trace { "Button up: ${controllerButton.name} - ${controller.asString()}" }
            switchInput(InputSource.CONTROLLER)
            return false
        }

        override fun axisMoved(controller: Controller, axisIndex: Int, value: Float): Boolean {
            if (abs(value) >= CONTROLLER_DEAD_ZONE) {
                switchInput(InputSource.CONTROLLER)
            }
            return false
        }

        private fun Controller.asString() =
            "'$name' id: $uniqueId player index: $playerIndex"

        private fun matchButton(index: Int) =
            ControllerButton.entries
                .find { entry -> entry.ordinal == index }
                ?: error("Unknown ControllerButton index: $index")
    }
}

enum class InputSource {
    KEYBOARD_MOUSE,
    CONTROLLER
}
