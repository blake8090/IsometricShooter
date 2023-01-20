package bke.iso.service

import bke.iso.service.container.ServiceContainer
import kotlin.reflect.KClass

class Provider<T : Any>(
    private val container: ServiceContainer,
    private val baseClass: KClass<T>
) {

    fun get(): T =
        container.get(baseClass)

    fun <U : T> get(kClass: KClass<U>): U =
        container.get(kClass)
}
