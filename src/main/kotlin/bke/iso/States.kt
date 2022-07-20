package bke.iso

import bke.iso.system.System

abstract class State {
    private val systems = this.getSystems()

    open fun start() {}

    open fun stop() {}

    open fun input(deltaTime: Float) {}

    open fun update(deltaTime: Float) {}

    fun updateSystems(deltaTime: Float) {
        systems.forEach { system -> system.update(deltaTime) }
    }

    open fun draw(deltaTime: Float) {}

    abstract fun getSystems(): Set<System>
}

class NoState : State() {
    override fun getSystems(): Set<System> = emptySet()
}
