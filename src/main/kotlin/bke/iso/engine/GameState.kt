package bke.iso.engine

interface System {
    fun update(deltaTime: Float)
}

abstract class GameState(game: bke.iso.engine.Game) {
    abstract val systems: Set<bke.iso.engine.System>

    open fun start() {}

    open fun update(deltaTime: Float) {}

    open fun stop() {}

    open fun handleEvent(event: bke.iso.engine.Event) {}
}

class EmptyState(game: bke.iso.engine.Game) : bke.iso.engine.GameState(game) {
    override val systems = emptySet<bke.iso.engine.System>()
}
