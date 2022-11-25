package bke.iso.engine.event

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import bke.iso.engine.Engine
import bke.iso.engine.log
import kotlin.reflect.KClass

@Service
class EventService(
    private val services: Services,
    private val engine: Engine
) {
    private val handlers = mutableMapOf<KClass<out Event>, MutableList<EventHandler<out Event>>>()

    fun <T : Event> addHandler(eventType: KClass<T>, handlerType: KClass<out EventHandler<T>>) {
        handlers.getOrPut(eventType) { mutableListOf() }
            .add(services.createInstance(handlerType))
        log.debug("Added handler '${handlerType.simpleName}' for event '${eventType.simpleName}'")
    }

    inline fun <reified T : Event> addHandler(handlerType: KClass<out EventHandler<T>>) =
        addHandler(T::class, handlerType)

    fun <T : Event> fire(event: T, type: KClass<T>) {
        for (eventHandler in getEventHandlers(type)) {
            eventHandler.handle(engine.deltaTime, event)
        }
    }

    inline fun <reified T : Event> fire(event: T) =
        fire(event, T::class)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> getEventHandlers(type: KClass<T>): List<EventHandler<T>> {
        val eventHandlers = handlers[type] ?: emptyList()
        if (eventHandlers.isEmpty()) {
            throw IllegalArgumentException("No handlers defined for event '${type.simpleName}'")
        }
        return eventHandlers.map { handler -> handler as EventHandler<T> }
    }
}
