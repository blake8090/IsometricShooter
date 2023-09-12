package bke.iso.engine

interface System {
    fun update(deltaTime: Float)
}

abstract class State {

    protected abstract val game: Game
    abstract val systems: Set<System>

    // TODO: make suspend and combine into start
    open fun load() {}

    open fun start() {}

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
