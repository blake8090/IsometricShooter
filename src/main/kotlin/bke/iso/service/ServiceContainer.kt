package bke.iso.service

import kotlin.reflect.KClass
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

    fun <T : Any> get(kClass: KClass<T>): T {
        val service = services[kClass] ?: throw MissingServiceError()
        val instance = when (service.lifetime) {
            Lifetime.SINGLETON -> service.instance ?: throw Error()
            Lifetime.TRANSIENT -> createInstance(service)
        }
        return kClass.cast(instance)
    }

    inline fun <reified T : Any> get() =
        get(T::class)

    fun <T : Any> getProvider(kClass: KClass<T>): Provider<T> =
        Provider(this, kClass)

    private fun <T : Any> createService(kClass: KClass<T>, createdServices: MutableSet<KClass<*>>): Service<*> {
        if (!createdServices.add(kClass)) {
            val str = createdServices
                .map { service -> service.simpleName }
                .joinToString()
            throw Error("circular dependency: $str -> ${kClass.simpleName}")
        }

        val lifetime =
            if (kClass.hasAnnotation<Singleton>()) {
                Lifetime.SINGLETON
            } else if (kClass.hasAnnotation<Transient>()) {
                Lifetime.TRANSIENT
            } else {
                throw Error()
            }
        val dependencies = kClass.primaryConstructor!!
            .parameters
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
        return Dependency(
            services.getOrPut(serviceClass) { createService(serviceClass, createdServices) },
            isProvider
        )
    }

    private fun <T : Any> initialize(service: Service<T>) {
        if (service.lifetime == Lifetime.SINGLETON) {
            service.instance = createInstance(service)
        }
        service.initialized = true
    }

    private fun <T : Any> resolveDependency(dependency: Dependency<T>): Any {
        val service = dependency.service
        if (!service.initialized) {
            initialize(service)
        }
        return if (dependency.isProvider) {

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
