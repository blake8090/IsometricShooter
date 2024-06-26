package bke.iso.engine.state

import bke.iso.engine.Event
import bke.iso.engine.Game

interface System {
    fun update(deltaTime: Float)
}

interface Module {
    fun update(deltaTime: Float)
    fun handleEvent(event: Event)
}

abstract class State {

    protected abstract val game: Game
    protected abstract val systems: LinkedHashSet<System>
    protected abstract val modules: Set<Module>

    open suspend fun load() {}

    open fun update(deltaTime: Float) {
        for (system in systems) {
            system.update(deltaTime)
        }
        for (module in modules) {
            module.update(deltaTime)
        }
    }

    open fun handleEvent(event: Event) {
        for (module in modules) {
            module.handleEvent(event)
        }
    }
}


