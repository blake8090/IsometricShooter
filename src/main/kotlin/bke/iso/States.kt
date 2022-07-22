package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.system.System
import bke.iso.util.getLogger
import kotlin.reflect.KClass

abstract class State {
    private val log = getLogger()

    val eventHandlers = EventHandlers()
    private val systems = mutableSetOf<System>()

    protected open fun getSystems(): Set<KClass<out System>> = emptySet()

    fun setup(container: ServiceContainer) {
        val types = getSystems()
        if (types.isNotEmpty()) {
            log.debug("Loading systems: $types")
            systems.clear()
            types.map(container::createInstance)
                .forEach { system ->
                    system.init()
                    systems.add(system)
                }
        }
    }

    fun updateSystems(deltaTime: Float) =
        systems.forEach { system -> system.update(deltaTime) }

    open fun start() {}

    open fun stop() {}

    open fun input(deltaTime: Float) {}

    open fun update(deltaTime: Float) {}

    open fun draw(deltaTime: Float) {}
}

class EmptyState : State()
