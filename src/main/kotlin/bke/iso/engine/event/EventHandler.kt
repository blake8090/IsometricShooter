package bke.iso.engine.event

open class Event

interface EventHandler<T : Event> {
    fun handle(deltaTime: Float, event: T)
}
