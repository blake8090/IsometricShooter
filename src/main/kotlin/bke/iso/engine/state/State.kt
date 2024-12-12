package bke.iso.engine.state

import bke.iso.engine.core.Event
import bke.iso.engine.Engine
import bke.iso.engine.core.Module

interface System {
    fun update(deltaTime: Float)
}

abstract class State {

    protected abstract val engine: Engine
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
