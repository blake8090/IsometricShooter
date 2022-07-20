package bke.iso

import kotlin.reflect.KClass

typealias EventHandler = (KClass<*>) -> Unit

open class Event
