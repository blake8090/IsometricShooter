package bke.iso.engine.input

import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.input.source.ControllerSource
import bke.iso.engine.input.source.KeyboardMouseSource
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.Controllers
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton
import mu.KotlinLogging

class Input(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    private val keyboardMouseSource = KeyboardMouseSource()
    private val controllerSource = ControllerSource()
    private var controllerConnected = false

    fun start() {
        Controllers.addListener(ControllerHandler())
    }

    override fun update(deltaTime: Float) {
        keyboardMouseSource.update()
        if (controllerConnected) {
            controllerSource.update()
        }
    }

    // TODO: improve binding api
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

    // TODO: improve this! maybe add a new binding for double binds between -1 and 1?
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

    inner class ControllerHandler : ControllerAdapter() {

        override fun connected(controller: Controller) {
            log.info { "Controller connected: '${controller.name}' id: ${controller.uniqueId}" }
        }

        override fun disconnected(controller: Controller) {
            log.info { "Controller disconnected: '${controller.name}' id: ${controller.uniqueId}" }
        }

        override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean {
            val controllerButton = matchButton(buttonIndex)
            log.trace { "Button down: ${controllerButton.name} - ${controller.log()}" }
            return false
        }

        override fun buttonUp(controller: Controller, buttonIndex: Int): Boolean {
            val controllerButton = matchButton(buttonIndex)
            log.trace { "Button up: ${controllerButton.name} - ${controller.log()}" }
            return false
        }

        override fun axisMoved(controller: Controller, axisIndex: Int, value: Float): Boolean {
            val controllerAxis = matchAxis(axisIndex)
            log.trace { "Axis '${controllerAxis.name}': $value - ${controller.log()}" }
            return false
        }

        private fun Controller.log() =
            "'$name' id: $uniqueId"

        private fun matchButton(index: Int) =
            ControllerButton.entries
                .find { entry -> entry.ordinal == index }
                ?: error("Unknown ControllerButton index: $index")

        private fun matchAxis(index: Int) =
            ControllerAxis.entries
                .find { entry -> entry.ordinal == index }
                ?: error("Unknown ControllerAxis index: $index")
    }
}
