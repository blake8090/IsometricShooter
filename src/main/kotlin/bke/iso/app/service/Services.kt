package bke.iso.app.service

import bke.iso.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Services {
    private val cache = ServiceCache()

    init {
        cache.add(Services::class) { this }
    }

    fun <T : Any> register(type: KClass<T>) {
        cache.add(type) { createInstance(type) }
        log.debug("Registered service '${type.simpleName}'")
    }

    fun <T : Any> get(type: KClass<T>): T =
        cache.get(type)

    inline fun <reified T : Any> get(): T =
        get(T::class)

    fun <T : Any> createInstance(type: KClass<T>): T {
        val constructor = type.primaryConstructor
            ?: throw IllegalArgumentException("Expected constructor for class '${type.simpleName}'")

        val dependencies = constructor.parameters
            .map { parameter -> parameter.type.classifier as KClass<*> }
            .map { dependencyType -> cache.get(dependencyType) }
            .toTypedArray()

        return constructor.call(*dependencies)
    }
}
