package bke.iso.service

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.cast
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

class ServiceContainer(classes: Set<KClass<*>>) {

    private val services = mutableMapOf<KClass<*>, Service<*>>()

    init {
        classes.forEach { kClass ->
            services[kClass] = createService(kClass, mutableSetOf())
            services.values.forEach { service -> initialize(service) }
        }
    }

    /**
     * Returns an instance of the given [kClass].
     *
     * @throws [NoServiceFoundException] if no service found for [kClass]
     */
    fun <T : Any> get(kClass: KClass<T>): T {
        val service = services[kClass] ?: throw NoServiceFoundException("No service found for class ${kClass.simpleName}")
        val instance = when (service.lifetime) {
            Lifetime.SINGLETON -> service.instance ?: throw RuntimeException("Expected instance")
            Lifetime.TRANSIENT -> createInstance(service)
        }
        return kClass.cast(instance)
    }

    /**
     * Returns an instance of the given class [T].
     *
     * @throws [NoServiceFoundException] if no service found for class [T]
     */
    inline fun <reified T : Any> get() =
        get(T::class)

    /**
     * Returns a [Provider] of class [kClass].
     *
     * @throws [NoServiceFoundException] if no service found for class [T]
     */
    fun <T : Any> getProvider(kClass: KClass<T>): Provider<T> {
        if (!services.containsKey(kClass)) {
            throw NoServiceFoundException("No service found for class ${kClass.simpleName}")
        }
        return Provider(this, kClass)
    }

    /**
     * Returns a [Provider] of class [T].
     *
     * @throws [NoServiceFoundException] if no service found for class [T]
     */
    inline fun <reified T : Any> getProvider() =
        getProvider(T::class)

    private fun <T : Any> createService(kClass: KClass<T>, createdServices: MutableSet<KClass<*>>): Service<*> {
        if (!createdServices.add(kClass)) {
            val names = createdServices
                .map { service -> service.simpleName }
                .joinToString()
            throw CircularDependencyException("Found circular dependency: $names -> ${kClass.simpleName}")
        }

        val lifetime =
            if (kClass.hasAnnotation<Singleton>()) {
                Lifetime.SINGLETON
            } else if (kClass.hasAnnotation<Transient>()) {
                Lifetime.TRANSIENT
            } else {
                throw MissingAnnotationsException("No annotations found for class ${kClass.simpleName}")
            }

        val dependencies = kClass.primaryConstructor!!
            .parameters
            .filter { parameter -> parameter.kind == KParameter.Kind.VALUE }
            .map { parameter -> createDependency(parameter.type, createdServices) }
            .toSet()

        return Service(
            kClass,
            kClass,
            lifetime,
            dependencies
        )
    }

    private fun createDependency(kType: KType, createdServices: MutableSet<KClass<*>>): Dependency<*> {
        var isProvider = false
        val serviceClass =
            if (kType.classifier == Provider::class) {
                isProvider = true
                kType.arguments.first()
                    .type!!
                    .classifier as KClass<*>
            } else {
                kType.classifier as KClass<*>
            }
        val service = services.getOrPut(serviceClass) {
            createService(serviceClass, createdServices)
        }
        return Dependency(service, isProvider)
    }

    private fun <T : Any> initialize(service: Service<T>) {
        if (service.lifetime == Lifetime.SINGLETON) {
            service.instance = createInstance(service)
        }
        service.initialized = true
    }

    private fun <T : Any> resolveDependency(dependency: Dependency<T>): Any {
        val service = dependency.service
        return if (dependency.isProvider) {
            getProvider(service.implClass)
        } else {
            get(service.implClass)
        }
    }

    private fun <T : Any> createInstance(service: Service<T>): T {
        val dependencies = service.dependencies
            .map { dependency -> resolveDependency(dependency) }
            .toTypedArray()
        return service.implClass.primaryConstructor!!.call(*dependencies)
    }
}

private data class Service<T : Any>(
    val baseClass: KClass<T>,
    val implClass: KClass<out T>,
    val lifetime: Lifetime,
    val dependencies: Set<Dependency<*>>
) {
    var instance: T? = null
    var initialized = false
}

private data class Dependency<T : Any>(
    val service: Service<T>,
    val isProvider: Boolean
)

private enum class Lifetime {
    SINGLETON,
    TRANSIENT
}
