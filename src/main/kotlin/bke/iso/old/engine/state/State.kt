package bke.iso.old.engine.state

import bke.iso.old.engine.event.EventHandler
import bke.iso.old.engine.event.EventHandlerMap
import bke.iso.old.engine.system.System
import bke.iso.old.service.ServiceProvider
import bke.iso.old.service.TransientService
import kotlin.reflect.KClass

abstract class State : TransientService {
    val eventHandlers = EventHandlerMap()
    val systems = mutableListOf<System>()

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}

    protected fun addHandlers(provider: ServiceProvider<EventHandler<*>>, vararg types: KClass<out EventHandler<*>>) {
        for (type in types) {
            eventHandlers.add(provider.get(type))
        }
    }

    protected fun addSystems(provider: ServiceProvider<System>, vararg types: KClass<out System>) {
        for (type in types) {
            systems.add(provider.get(type))
        }
    }
}

class EmptyState : State()
