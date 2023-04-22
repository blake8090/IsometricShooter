package bke.iso.engine.event

import bke.iso.service.TransientService
import kotlin.reflect.KClass

open class Event

interface EventHandler<T : Event> : TransientService {
    val type: KClass<T>

    fun handle(event: T)
}
