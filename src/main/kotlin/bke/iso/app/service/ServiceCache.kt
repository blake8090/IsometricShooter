package bke.iso.app.service

import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class MissingServiceException(message: String) : RuntimeException(message)

class ServiceCache {
    private val services = mutableMapOf<KClass<*>, Lazy<*>>()

    fun <T : Any> add(type: KClass<T>, provider: () -> T) {
        services[type] = lazy(provider)
    }

    fun <T : Any> get(type: KClass<T>): T {
        val instance = services[type]
            ?.value
            ?: throw MissingServiceException("Service ${type.simpleName} was not found")
        return type.safeCast(instance)
            ?: throw IllegalArgumentException(
                "Instance ${instance.javaClass.simpleName} is not of type ${type.simpleName}"
            )
    }
}
