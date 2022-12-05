package bke.iso.engine.event

import kotlin.reflect.KClass

class EventHandlerMap {
    private val handlersByType = mutableMapOf<KClass<out Event>, MutableList<EventHandler<out Event>>>()

    fun <T : Event> add(type: KClass<out T>, eventHandler: EventHandler<out T>) {
        handlersByType.getOrPut(type) { mutableListOf() }
            .add(eventHandler)
    }

    inline fun <reified T : Event> add(eventHandler: EventHandler<out T>) {
        add(T::class, eventHandler)
    }

    fun clear() =
        handlersByType.clear()

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> fire(event: T, type: KClass<out T>) {
        val handlers = handlersByType[type] ?: emptyList()
        if (handlers.isEmpty()) {
            throw IllegalArgumentException("No handlers defined for event '${type.simpleName}'")
        }
        for (handler in handlers) {
            (handler as EventHandler<T>).handle(event)
        }
    }
}
