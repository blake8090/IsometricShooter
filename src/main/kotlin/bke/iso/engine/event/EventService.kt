package bke.iso.engine.event

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import kotlin.reflect.KClass

@Service
class EventService(private val services: Services) {
    private val handlers = mutableMapOf<KClass<out Event>, MutableList<EventHandler<out Event>>>()

    fun <T : Event> addHandler(eventType: KClass<T>, handlerType: KClass<out EventHandler<T>>) {
        handlers.getOrPut(eventType) { mutableListOf() }
            .add(services.createInstance(handlerType))
    }

    inline fun <reified T : Event> addHandler(handlerType: KClass<out EventHandler<T>>) =
        addHandler(T::class, handlerType)

    fun <T : Event> fire(event: T, type: KClass<T>) {
        getEventHandlers(type)
            .forEach { handler -> handler.handle(0f, event) }
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
