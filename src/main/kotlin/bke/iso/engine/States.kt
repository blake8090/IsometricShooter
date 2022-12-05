package bke.iso.engine

import bke.iso.engine.event.Event
import bke.iso.engine.event.EventHandler
import kotlin.reflect.KClass

abstract class State {
    abstract val controllers: Set<KClass<out Controller>>
    abstract val eventHandlers: Set<KClass<out EventHandler<out Event>>>

    open fun start() {}

    open fun stop() {}
}

class EmptyState : State() {
    override val controllers: Set<KClass<out Controller>> = emptySet()
    override val eventHandlers: Set<KClass<out EventHandler<out Event>>> = emptySet()
}
