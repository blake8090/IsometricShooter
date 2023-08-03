package bke.iso.v2.engine

import bke.iso.engine.event.Event

interface System {
    fun update(deltaTime: Float)
}

abstract class GameState(game: Game) {
    abstract val systems: Set<System>

    open fun start() {}

    open fun update(deltaTime: Float) {}

    open fun stop() {}

    open fun handleEvent(event: Event) {}
}

class EmptyState(game: Game) : GameState(game) {
    override val systems = emptySet<System>()
}
