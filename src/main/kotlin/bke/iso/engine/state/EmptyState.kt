package bke.iso.engine.state

import bke.iso.engine.Game

class EmptyState(override val game: Game) : State() {
    override val systems: LinkedHashSet<System> = linkedSetOf()
    override val modules: Set<Module> = emptySet()
}
