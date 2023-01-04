package bke.iso.service

import kotlin.reflect.KClass

class Provider<T : Any>(private val serviceContainer: ServiceContainer) {

    fun <U : T> get(subType: KClass<U>): U =
        serviceContainer.get(subType)
}

inline fun <reified T : Any> Provider<T>.get() =
    get(T::class)

inline fun <reified T : Any> Provider<T>.type() =
    T::class
