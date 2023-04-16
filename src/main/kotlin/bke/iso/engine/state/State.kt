package bke.iso.engine.state

import bke.iso.engine.event.EventHandler
import bke.iso.engine.event.EventHandlerMap
import bke.iso.engine.system.System
import bke.iso.service.Provider
import kotlin.reflect.KClass

abstract class State {
    val eventHandlers = EventHandlerMap()
    val systems = mutableListOf<System>()

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}

    protected fun addHandlers(provider: Provider<EventHandler<*>>, vararg types: KClass<out EventHandler<*>>) {
        for (type in types) {
            eventHandlers.add(provider.get(type))
        }
    }

    protected fun addSystems(provider: Provider<System>, vararg types: KClass<out System>) {
        for (type in types) {
            systems.add(provider.get(type))
        }
    }
}

class EmptyState : State()
