package bke.iso.engine.event

open class Event

interface EventHandler<T : Event> {
    fun handle(event: T)
}
