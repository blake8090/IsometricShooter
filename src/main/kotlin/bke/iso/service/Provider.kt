package bke.iso.service

import bke.iso.service.cache.ServiceCache
import kotlin.reflect.KClass

class Provider<T : Any>(
    private val cache: ServiceCache,
    private val baseClass: KClass<T>
) {

    fun get(): T =
        cache[baseClass]

    fun <U : T> get(kClass: KClass<U>): U =
        cache[kClass]
}
