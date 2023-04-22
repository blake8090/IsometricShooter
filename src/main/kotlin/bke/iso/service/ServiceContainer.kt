package bke.iso.service

import kotlin.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class ServiceContainer {

    private val graph = ServiceGraph()
    private val creator = ServiceCreator(this, graph)

    /**
     * Registers all provided types as services.
     * Instances are immediately created for all [SingletonService]s.
     *
     * @throws RegisterServiceException when an exception occurs while registering a service
     */
    fun <T : Service> register(types: Set<KClass<out T>>) {
        for (type in types) {
            try {
                register(type)
            } catch (e: Exception) {
                throw RegisterServiceException(type, e)
            }
        }

        // before calling all the create methods, we have to make sure all singleton instances are initialized.
        // this avoids issues with singleton services getting *other* singleton services from a ServiceProvider.
        graph.getNodes().forEach{ node -> initialize(node) }

        graph.getNodes()
            .mapNotNull { it.instance }
            .forEach { it.create() }
    }

    /**
     * Registers all provided types as services.
     * Instances are immediately created for all [SingletonService]s.
     *
     * @throws RegisterServiceException when an exception occurs while registering a service
     */
    fun <T : Service> register(vararg types: KClass<out T>) =
        register(types.toSet())

    private fun register(service: KClass<out Service>) {
        if (graph.contains(service)) {
            return
        }

        graph.add(service)

        for (parameter in service.primaryConstructor!!.parameters) {
            val kClass = parameter.type.jvmErasure
            if (kClass.isSubclassOf(Service::class)) {
                @Suppress("UNCHECKED_CAST")
                register(kClass as KClass<out Service>)
                graph.link(service to kClass)
            } else if (kClass != ServiceProvider::class) {
                throw InvalidDependencyException(
                    "Parameter '${parameter.name}' must be"
                            + " either a Service or a ServiceProvider"
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

        node.instance = creator.createInstance(node.type)
    }

    /**
     * Returns an instance of a [Service].
     *
     * @throws IllegalStateException if an instance for a [SingletonService] was not found
     */
    fun <T : Service> get(type: KClass<T>): T {
        val node = graph.get(type)
        return if (type.isSubclassOf(SingletonService::class)) {
            node.instance ?: throw IllegalStateException("Expected instance of singleton service ${type.simpleName}")
        } else {
            return createInstance(type)
        }
    }

    /**
     * Returns an instance of a [Service].
     *
     * @throws IllegalStateException if an instance for a [SingletonService] was not found
     */
    inline fun <reified T : Service> get() =
        get(T::class)

    private fun <T : Service> createInstance(type: KClass<T>): T {
        val instance = creator.createInstance(type)
        instance.create()
        return instance
    }

    /**
     * Calls [Service.dispose] for all service instances.
     */
    fun dispose() {
        graph.getNodes()
            .mapNotNull(Node<*>::instance)
            .forEach(Service::dispose)
    }
}
