package bke.iso.engine.state

import bke.iso.engine.Engine
import bke.iso.engine.core.Module

class EmptyState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = linkedSetOf()
    override val modules: Set<Module> = emptySet()
}
