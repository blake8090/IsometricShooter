package bke.iso.engine.input

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import mu.KotlinLogging
import kotlin.math.abs

const val CONTROLLER_DEAD_ZONE = 0.2f

class Input(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    private val keyboardMouseBindings = Bindings()
    private val controllerBindings = Bindings()
    private val inputMultiplexer = InputMultiplexer()
    private val inputState = InputState(game.events)

    init {
        keyboardMouseBindings.isButtonDown = (::checkButtonDown)
        controllerBindings.isButtonDown = (::checkButtonDown)
        controllerBindings.getAxis = (::getAxis)
    }

    override fun start() {
        Gdx.input.inputProcessor = inputMultiplexer
        addInputProcessor(inputState.keyMouseHandler)
        Controllers.addListener(inputState.controllerHandler)
    }

    override fun update(deltaTime: Float) {
        keyboardMouseBindings.update()
        controllerBindings.update()
    }

    fun isUsingController() =
        inputState.usingController

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
        return if (inputState.usingController) {
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

    fun addControllerListener(listener: ControllerListener) =
        Controllers.addListener(listener)

    fun removeControllerListener(listener: ControllerListener) =
        Controllers.removeListener(listener)

    private fun checkButtonDown(binding: ButtonBinding) =
        when (binding) {
            is KeyBinding -> Gdx.input.isKeyPressed(binding.code)
            is MouseBinding -> Gdx.input.isButtonPressed(binding.code)
            is ControllerBinding -> inputState.findPrimaryController()
                ?.getButton(binding.code)
                ?: false
        }

    private fun getAxis(binding: AxisBinding): Float {
        val axis = inputState.findPrimaryController()
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

    class OnControllerConnect : Event()
}
