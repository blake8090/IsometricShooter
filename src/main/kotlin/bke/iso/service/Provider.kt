package bke.iso.service

import kotlin.reflect.KClass

// TODO: rename to ServiceProvider for consistency
class Provider<T : Any> internal constructor(
    private val cache: ServiceCache,
    private val baseClass: KClass<T>
) {

    fun get(): T =
        cache[baseClass]

    fun <U : T> get(kClass: KClass<U>): U =
        cache[kClass]
}
