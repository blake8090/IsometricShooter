package bke.iso.service.container

import kotlin.reflect.KClass

internal enum class Lifetime {
    SINGLETON,
    TRANSIENT
}

internal data class ServiceRecord<T : Any>(
    val kClass: KClass<T>,
    val lifetime: Lifetime,
    val dependencies: List<() -> ServiceInstance<*>>,
    var initialized: Boolean = false
)
