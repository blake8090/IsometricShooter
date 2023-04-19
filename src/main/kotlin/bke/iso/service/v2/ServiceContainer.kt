package bke.iso.service.v2

import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

// TODO: write Javadocs
class ServiceContainer {

    private val graph = ServiceGraph()

    /**
     *
     */
    fun <T : Service> register(vararg types: KClass<out T>) {
        for (type in types) {
            if (!graph.contains(type)) {
                register(type)
            }
        }
        for (node in graph.getNodes()) {
            initialize(node)
        }
    }

    private fun <T : Service> register(type: KClass<T>) {
        graph.add(type)

        val dependencies = getDependencies(type)
        validateDependencies(type, dependencies)

        val services = dependencies.filterIsInstance<KClass<Service>>()
        for (service in services) {
            if (!graph.contains(service)) {
                register(service)
            }
            graph.link(type to service)
        }
    }

    private fun <T : Service> getDependencies(type: KClass<T>): List<KClass<*>> =
        type.primaryConstructor!!.parameters
            .map(KParameter::type)
            .map(KType::jvmErasure)

    private fun <T : Service> validateDependencies(type: KClass<T>, dependencies: List<KClass<*>>) {
        for (dependency in dependencies) {
            if (!dependency.isSubclassOf(Service::class)) {
                throw Error("${type.simpleName} failed validation: Dependency ${dependency.simpleName} is not a service")
            }
        }
    }

    private fun <T : Service> initialize(node: Node<T>) {
        if (!node.type.isSubclassOf(SingletonService::class) || node.instance != null) {
            return
        }

        // to avoid null references, all singleton dependencies need to be initialized first!
        node.links.filterIsInstance<KClass<SingletonService>>()
            .map(graph::get)
            .forEach { link -> initialize(link) }

        val type = node.type
        val instance = createInstance(type)
        node.instance = instance
    }

    /**
     *
     */
    fun <T : Service> get(type: KClass<T>): T {
        val node = graph.get(type)
        return if (type.isSubclassOf(SingletonService::class)) {
            node.instance ?: throw Error("Expected instance of singleton service ${type.simpleName}")
        } else {
            createInstance(type)
        }
    }

    inline fun <reified T : Service> get() =
        get(T::class)

    @Suppress("UNCHECKED_CAST")
    private fun resolveDependency(dependency: KClass<*>): Any {
        if (dependency.isSubclassOf(TransientService::class)) {
            return createInstance(dependency as KClass<Service>)
        } else if (dependency.isSubclassOf(SingletonService::class)) {
            val node = graph.get(dependency as KClass<Service>)
            return node.instance!!
        }
        return Any()
    }

    private fun <T : Service> createInstance(type: KClass<out T>): T =
        try {
            val params = getDependencies(type)
                .map(::resolveDependency)
                .toTypedArray()

            if (params.isEmpty()) {
                type.createInstance()
            } else {
                type.primaryConstructor!!.call(*params)
            }
        } catch (e: Exception) {
            throw RuntimeException("Error creating instance of ${type.simpleName}", e)
        }
}
