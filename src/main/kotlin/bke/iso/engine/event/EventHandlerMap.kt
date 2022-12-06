package bke.iso.engine.event

import kotlin.reflect.KClass

class EventHandlerMap {
    private val handlersByType = mutableMapOf<KClass<out Event>, MutableList<EventHandler<out Event>>>()

    fun <T : Event> add(handler: EventHandler<T>) {
        handlersByType.getOrPut(handler.type) { mutableListOf() }
            .add(handler)
    }

    fun clear() =
        handlersByType.clear()

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> fire(event: T, type: KClass<out T>) {
        val handlers = handlersByType[type] ?: emptyList()
        for (handler in handlers) {
            (handler as EventHandler<T>).handle(event)
        }
    }
}
