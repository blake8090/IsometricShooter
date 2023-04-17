package bke.iso.service.v2

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class ServiceContainer {

    private val graph = ServiceGraph()

    fun <T : Service> register(type: KClass<out T>) {
        graph.add(type)

        val serviceDependencies = getDependencyTypes(type)
            .filterIsInstance<KClass<out Service>>()

        for (dependency in serviceDependencies) {
            if (!graph.contains(dependency)) {
                // todo: initialization chain for logging?
                register(dependency)
            }
            graph.link(type to dependency)
        }
    }

    fun <T : Service> createInstance(type: KClass<out T>): T =
        try {
            val params = getDependencyTypes(type)
                .map(::resolveDependency)
                .toTypedArray()
            if (params.isEmpty()) {
                type.createInstance()
            } else {
                type.primaryConstructor!!.call(*params)
            }
        } catch (e: Exception) {
            throw Error("error creating instance of ${type.simpleName}: ${e.localizedMessage}")
        }

    @Suppress("UNCHECKED_CAST")
    private fun resolveDependency(dependency: KClass<*>): Any {
        if (dependency.isSubclassOf(TransientService::class)) {
            return createInstance(dependency as KClass<out Service>)
        } else if (dependency.isSubclassOf(SingletonService::class)) {
            val node = graph.get(dependency as KClass<out Service>)
            return node.instance!!
        }
        return Any()
    }

    private fun <T : Service> getDependencyTypes(type: KClass<out T>): List<KClass<*>> =
        type.primaryConstructor!!.parameters
            .map(KParameter::type)
            .map(KType::jvmErasure)
}
