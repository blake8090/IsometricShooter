package bke.iso.service.container

import kotlin.reflect.KClass

internal enum class Lifetime {
    SINGLETON,
    TRANSIENT
}

// TODO: rename to ServiceRecord?
internal data class Record<T : Any>(
    val kClass: KClass<T>,
    val lifetime: Lifetime,
    val dependencies: List<() -> Instance<*>>,
    var initialized: Boolean = false
)
