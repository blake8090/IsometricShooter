package bke.iso.engine

import bke.iso.engine.ui.UIScreen

abstract class System {
    abstract fun update(deltaTime: Float)
}

abstract class State {

    protected abstract val game: Game
    abstract val systems: Set<System>

    open var loadingScreen: UIScreen? = null
        protected set

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
    override val systems = emptySet<System>()
}
