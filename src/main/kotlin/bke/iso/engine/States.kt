package bke.iso.engine

import kotlin.reflect.KClass

abstract class State {
    abstract val controllers: Set<KClass<out Controller>>

    open fun start() {}

    open fun stop() {}
}

class EmptyState : State() {
    override val controllers: Set<KClass<out Controller>> = emptySet()
}
