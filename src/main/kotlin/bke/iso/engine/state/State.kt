package bke.iso.engine.state

import bke.iso.engine.event.EventHandlerMap
import bke.iso.engine.system.System

abstract class State {
    val eventHandlers = EventHandlerMap()
    val systems = mutableListOf<System>()

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}
}

class EmptyState : State()