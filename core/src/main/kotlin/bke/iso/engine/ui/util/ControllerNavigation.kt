package bke.iso.engine.ui.util

import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.utils.Pools
import com.studiohartman.jamepad.ControllerButton
import io.github.oshai.kotlinlogging.KotlinLogging

class ControllerNavigation : ControllerAdapter() {

    private val log = KotlinLogging.logger {}

    private val actors = mutableListOf<Actor>()
    private var pointer = 0

    fun add(actor: Actor) {
        actors.add(actor)
    }

    fun start() {
        selectButton()
    }

    override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean =
        when (ControllerButton.entries[buttonIndex]) {
            ControllerButton.A -> clickButton()
            else -> false
        }

    override fun buttonUp(controller: Controller, buttonIndex: Int): Boolean =
        when (ControllerButton.entries[buttonIndex]) {
            ControllerButton.A -> releaseButton()
            ControllerButton.DPAD_UP -> moveUp()
            ControllerButton.DPAD_DOWN -> moveDown()
            else -> false
        }

    private fun fireEvent(actor: Actor, type: InputEvent.Type, code: Int? = null): Boolean {
        val event = Pools.obtain(InputEvent::class.java)
        event.type = type
        event.pointer = -1 // -1 is always used on desktop
        if (code != null) {
            event.button = code
        }
        log.debug { "Firing event $event on actor $actor" }
        actor.fire(event)
        val handled = event.isHandled
        Pools.free(event)
        return handled
    }

    private fun clickButton(): Boolean {
        val button = actors.getOrNull(pointer) ?: return false
        return fireEvent(button, InputEvent.Type.touchDown, Input.Buttons.LEFT)
    }

    private fun releaseButton(): Boolean {
        val button = actors.getOrNull(pointer) ?: return false
        return fireEvent(button, InputEvent.Type.touchUp, Input.Buttons.LEFT)
    }

    private fun moveUp(): Boolean {
        if (actors.size <= 1) {
            return false
        }
        unSelectButton()
        pointer--
        if (pointer < 0) {
            pointer = actors.size - 1
        }
        return selectButton()
    }

    private fun moveDown(): Boolean {
        if (actors.size <= 1) {
            return false
        }
        unSelectButton()
        pointer++
        if (pointer >= actors.size) {
            pointer = 0
        }
        return selectButton()
    }

    private fun selectButton(): Boolean {
        val button = actors.getOrNull(pointer) ?: return false
        return fireEvent(button, InputEvent.Type.enter)
    }

    private fun unSelectButton(): Boolean {
        val button = actors.getOrNull(pointer) ?: return false
        return fireEvent(button, InputEvent.Type.exit)
    }
}
