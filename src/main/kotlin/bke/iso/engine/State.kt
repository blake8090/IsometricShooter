package bke.iso.engine

interface System {
    fun update(deltaTime: Float)
}

abstract class State {

    protected abstract val game: Game
    abstract val systems: Set<System>

    open suspend fun load() {}

    open fun update(deltaTime: Float) {
        for (system in systems) {
            system.update(deltaTime)
        }
    }

    open fun handleEvent(event: Event) {}
}

class EmptyState(override val game: Game) : State() {
    override val systems: Set<System> = emptySet()
}
