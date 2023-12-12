package bke.iso.engine

interface System {
    fun update(deltaTime: Float)
}

interface Module {
    fun update(deltaTime: Float)
    fun handleEvent(event: Event)
}

abstract class State {

    protected abstract val game: Game
    protected abstract val systems: Set<System>
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

class EmptyState(override val game: Game) : State() {
    override val systems: Set<System> = emptySet()
    override val modules: Set<Module> = emptySet()
}
