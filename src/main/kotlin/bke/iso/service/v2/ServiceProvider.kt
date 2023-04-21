package bke.iso.service.v2

import kotlin.reflect.KClass

//todo: unit tests
class ServiceProvider<T : Service>(
    private val container: ServiceContainer,
    private val baseClass: KClass<T>
) {

    fun get(): T =
        container.get(baseClass)

    fun <U : T> get(kClass: KClass<U>): U =
        container.get(kClass)
}
