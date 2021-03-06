package bke.iso.engine

import kotlin.reflect.KClass

open class Event

interface EventHandler<T : Event> {
    fun handle(event: T)
}

class EventHandlers {
    private val handlersByType = mutableMapOf<KClass<out Event>, MutableSet<EventHandler<out Event>>>()

    fun <T : Event> handleEvent(type: KClass<T>, action: (T) -> Unit) {
        val handlers = handlersByType.getOrPut(type) { mutableSetOf() }
        handlers.add(
            object : EventHandler<T> {
                override fun handle(event: T) {
                    action.invoke(event)
                }
            })
    }

    inline fun <reified T : Event> handleEvent(noinline action: (T) -> Unit) =
        handleEvent(T::class, action)

    fun <T : Event> run(event: T, eventType: KClass<T>) {
        findHandlers(eventType)
            .forEach { eventHandler -> eventHandler.handle(event) }
    }

    inline fun <reified T : Event> run(event: T) =
        run(event, T::class)

    private fun <T : Event> findHandlers(eventType: KClass<T>): Set<EventHandler<T>> {
        return handlersByType[eventType]
            ?.filterIsInstance<EventHandler<T>>()
            ?.toSet()
            ?: emptySet()
    }
}
