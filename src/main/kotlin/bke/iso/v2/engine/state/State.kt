package bke.iso.v2.engine.state

import bke.iso.v2.engine.system.System

//import bke.iso.engine.Controller
//import bke.iso.engine.event.Event
//import bke.iso.engine.event.EventHandler

abstract class State {
//    abstract val controllers: Set<KClass<out Controller>>
//    abstract val eventHandlers: Set<KClass<out EventHandler<out Event>>>
    abstract val systems: Set<System>

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}
}

class EmptyState : State() {
    override val systems = emptySet<System>()
}
