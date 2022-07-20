package bke.iso

import bke.iso.system.System
import kotlin.reflect.KClass

abstract class State {
    open fun start() {}

    open fun stop() {}

    open fun input(deltaTime: Float) {}

    open fun update(deltaTime: Float) {}

    open fun draw(deltaTime: Float) {}

    abstract fun getSystems(): Set<KClass<System>>
}

class EmptyState : State() {
    override fun getSystems(): Set<KClass<System>> = emptySet()
}
