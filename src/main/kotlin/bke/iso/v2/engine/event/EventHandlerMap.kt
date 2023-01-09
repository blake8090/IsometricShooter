package bke.iso.v2.engine.event

import kotlin.reflect.KClass

class EventHandlerMap {
    private val handlersByType = mutableMapOf<KClass<out Event>, MutableList<EventHandler<out Event>>>()

    fun <T : Event> add(handler: EventHandler<T>) {
        handlersByType.getOrPut(handler.type) { mutableListOf() }
            .add(handler)
    }

    fun <T : Event> fire(event: T) {
        handlersByType[event::class]
            ?.filterIsInstance<EventHandler<T>>()
            ?.forEach { handler -> handler.handle(event) }
    }
}
