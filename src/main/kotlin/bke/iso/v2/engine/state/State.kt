package bke.iso.v2.engine.state

import bke.iso.v2.engine.event.EventHandlerMap
import bke.iso.v2.engine.system.System

abstract class State {
    val eventHandlers = EventHandlerMap()
    abstract val systems: Set<System>

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}
}

class EmptyState : State() {
    override val systems = emptySet<System>()
}
