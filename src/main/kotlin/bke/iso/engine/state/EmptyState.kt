package bke.iso.engine.state

import bke.iso.engine.Engine

class EmptyState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = linkedSetOf()
    override val modules: Set<Module> = emptySet()
}
