package bke.iso.service.v2

import kotlin.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

// TODO: write Javadocs
class ServiceContainer {

    private val graph = ServiceGraph()
    private val creator = ServiceCreator(this, graph)

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

    private fun register(service: KClass<out Service>) {
        graph.add(service)

        for (parameter in service.primaryConstructor!!.parameters) {
            val kClass = parameter.type.jvmErasure
            if (kClass.isSubclassOf(Service::class)) {
                @Suppress("UNCHECKED_CAST")
                register(kClass as KClass<out Service>)
                graph.link(service to kClass)
            } else if (kClass != ServiceProvider::class) {
                throw InvalidDependencyException(
                    "Error registering '${service.simpleName}': "
                            + "Parameter '${parameter.name}' must be either a Service or a ServiceProvider"
                )
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
        val instance = creator.createInstance(type)
        node.instance = instance
    }

    /**
     *
     */
    fun <T : Service> get(type: KClass<T>): T {
        val node = graph.get(type)
        return if (type.isSubclassOf(SingletonService::class)) {
            node.instance ?: throw IllegalStateException("Expected instance of singleton service ${type.simpleName}")
        } else {
            creator.createInstance(type)
        }
    }

    inline fun <reified T : Service> get() =
        get(T::class)
}
