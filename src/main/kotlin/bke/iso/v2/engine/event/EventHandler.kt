package bke.iso.v2.engine.event

import kotlin.reflect.KClass

open class Event

interface EventHandler<T : Event> {
    val type: KClass<T>

    fun handle(event: T)
}
