package bke.iso.engine.input

import bke.iso.engine.Game
import bke.iso.engine.Module
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.Controllers
import com.studiohartman.jamepad.ControllerButton
import mu.KotlinLogging
import kotlin.math.abs

private const val CONTROLLER_DEAD_ZONE = 0.2f

class Input(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    private val keyboardMouseBindings = Bindings()
    private val controllerBindings = Bindings()
    private val inputMultiplexer = InputMultiplexer()

    var usingController = false
        private set

    init {
        keyboardMouseBindings.isButtonDown = { binding ->
            checkButtonDown(binding)
        }

        controllerBindings.isButtonDown = { binding ->
            checkButtonDown(binding)
        }

        controllerBindings.getAxis = { binding ->
            getAxis(binding)
        }
    }

    fun start() {
        Gdx.input.inputProcessor = inputMultiplexer
        addInputProcessor(KeyMouseHandler())
        Controllers.addListener(ControllerHandler())
    }

    override fun update(deltaTime: Float) {
        keyboardMouseBindings.update()
        controllerBindings.update()
    }

    fun bind(vararg bindings: Pair<String, Binding>) {
        bindings.forEach { (action, binding) ->
            when (binding) {
                is KeyBinding, is MouseBinding -> {
                    keyboardMouseBindings[action] = binding
                }

                is ControllerBinding, is ControllerAxisBinding -> {
                    controllerBindings[action] = binding
                }

                else -> {}
            }
        }
    }

    fun bind(action: String, negative: KeyBinding, positive: KeyBinding) {
        keyboardMouseBindings[action] = CompositeBinding(negative, positive)
    }

    fun bind(action: String, negative: ControllerBinding, positive: ControllerBinding) {
        controllerBindings[action] = CompositeBinding(negative, positive)
    }

    fun poll(action: String): Float {
        return if (usingController && findController() != null) {
            controllerBindings.poll(action)
        } else {
            keyboardMouseBindings.poll(action)
        }
    }

    fun onAction(actionName: String, func: (Float) -> Unit) {
        val axis = poll(actionName)
        if (axis != 0f) {
            func.invoke(axis)
        }
    }

    fun addInputProcessor(processor: InputProcessor) {
        inputMultiplexer.addProcessor(0, processor)
    }

    fun removeInputProcessor(processor: InputProcessor) =
        inputMultiplexer.removeProcessor(processor)

    private fun checkButtonDown(binding: ButtonBinding) =
        when (binding) {
            is KeyBinding -> Gdx.input.isKeyPressed(binding.code)
            is MouseBinding -> Gdx.input.isButtonPressed(binding.code)
            is ControllerBinding ->
                Controllers.getCurrent()
                    ?.getButton(binding.code)
                    ?: false
        }

    private fun getAxis(binding: AxisBinding): Float {
        val axis = findController()
            ?.getAxis(binding.code)
            ?: return 0f

        if (abs(axis) <= CONTROLLER_DEAD_ZONE) {
            return 0f
        }

        return if (binding.invert) {
            axis * -1f
        } else {
            axis
        }
    }

    private fun switchInput(useController: Boolean) {
        if (useController && !usingController) {
            log.debug { "Switching input to controller" }
            usingController = true
        } else if (!useController && usingController) {
            log.debug { "Switching input to keyboard/mouse" }
            usingController = false
        }
    }

    private fun findController(): Controller? =
        Controllers.getCurrent()
            ?.takeIf(Controller::isConnected)

    inner class KeyMouseHandler : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            switchInput(false)
            return true
        }

        override fun keyUp(keycode: Int): Boolean {
            switchInput(false)
            return true
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            switchInput(false)
            return true
        }
    }

    inner class ControllerHandler : ControllerAdapter() {

        override fun connected(controller: Controller) {
            log.info { "Controller connected: ${controller.log()}" }
        }

        override fun disconnected(controller: Controller) {
            log.info { "Controller disconnected: ${controller.log()}" }
        }

        override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean {
            val controllerButton = matchButton(buttonIndex)
            log.trace { "Button down: ${controllerButton.name} - ${controller.log()}" }
            switchInput(true)
            return false
        }

        override fun buttonUp(controller: Controller, buttonIndex: Int): Boolean {
            val controllerButton = matchButton(buttonIndex)
            log.trace { "Button up: ${controllerButton.name} - ${controller.log()}" }
            switchInput(true)
            return false
        }

        override fun axisMoved(controller: Controller, axisIndex: Int, value: Float): Boolean {
            if (abs(value) >= CONTROLLER_DEAD_ZONE) {
                switchInput(true)
            }
            return false
        }

        private fun Controller.log() =
            "'$name' id: $uniqueId player index: $playerIndex"

        private fun matchButton(index: Int) =
            ControllerButton.entries
                .find { entry -> entry.ordinal == index }
                ?: error("Unknown ControllerButton index: $index")
    }
}
