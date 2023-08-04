package bke.iso.v2.engine

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
