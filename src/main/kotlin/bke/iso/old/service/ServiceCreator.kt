package bke.iso.old.service

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

// TODO: unit tests
class ServiceCreator(private val container: ServiceContainer, private val graph: ServiceGraph) {

    fun <T : Service> createInstance(type: KClass<T>): T =
        try {
            val params = type.primaryConstructor!!.parameters
                .map(KParameter::type)
                .map(::resolveDependency)
                .toTypedArray()

            if (params.isEmpty()) {
                type.createInstance()
            } else {
                type.primaryConstructor!!.call(*params)
            }
        } catch (e: Exception) {
            throw ServiceCreationException("Error creating instance of ${type.simpleName}", e)
        }

    @Suppress("UNCHECKED_CAST")
    private fun resolveDependency(type: KType): Any {
        val kClass = type.jvmErasure
        return if (kClass.isSubclassOf(Service::class)) {
            findServiceInstance(kClass as KClass<Service>)
        } else if (kClass == ServiceProvider::class) {
            createProviderInstance(type)
        } else {
            throw IllegalArgumentException("Cannot resolve dependency of unknown type ${type.jvmErasure.simpleName}")
        }
    }

    private fun <T : Service> findServiceInstance(kClass: KClass<T>): T =
        if (kClass.isSubclassOf(SingletonService::class)) {
            graph.get(kClass).instance!!
        } else {
            createInstance(kClass)
        }

    @Suppress("UNCHECKED_CAST")
    private fun createProviderInstance(type: KType): ServiceProvider<Service> {
        val parameterizedClass = type.arguments
            .first()
            .type!!
            .jvmErasure
        return ServiceProvider(container, parameterizedClass as KClass<Service>)
    }
}
