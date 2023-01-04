package bke.iso.service

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.safeCast

class ServiceContainer(services: Set<KClass<*>>) {

    private val services: Map<KClass<*>, ServiceData>
    private val singletons = mutableMapOf<KClass<*>, Any>()

    init {
        this.services = services.associateWith(this::toServiceData)
        initialize()
        validate()
    }

    private fun toServiceData(service: KClass<*>): ServiceData {
        val lifetime =
            if (service.hasAnnotation<Singleton>()) {
                Lifetime.SINGLETON
            } else if (service.hasAnnotation<Transient>()) {
                Lifetime.TRANSIENT
            } else {
                throw IllegalArgumentException("gotta have annotations")
            }

        // TODO: add support for implementing sub types
        return ServiceData(service, lifetime)
    }

    private fun initialize() {
        for ((service, data) in services) {
            if (data.lifetime == Lifetime.SINGLETON) {
                singletons[service] = createInstance(service)
            }
        }
    }

    private fun validate() {
        for ((service, data) in services) {
            if (data.lifetime == Lifetime.SINGLETON && !singletons.containsKey(service)) {
                throw IllegalArgumentException("Singleton '${service.simpleName}' does not have an instance")
            }
            getPrimaryConstructor(service)
                .parameters
                .map { parameter -> parameter.type.classifier }
                .filterIsInstance<KClass<*>>()
                .forEach(this::validate)
        }
    }

    private fun validate(dependency: KClass<*>) {
        if (dependency == Provider::class) {
            // TODO: validate provider
            return
        }

        val data = services[dependency]
            ?: throw IllegalArgumentException("'${dependency.simpleName}' was not registered")

        if (data.lifetime == Lifetime.SINGLETON && !singletons.containsKey(dependency)) {
            throw IllegalArgumentException("Singleton '${dependency.simpleName}' does not have an instance")
        }
    }

    private fun <T : Any> getPrimaryConstructor(type: KClass<T>): KFunction<T> =
        type.primaryConstructor
            ?: throw IllegalArgumentException("Expected constructor for class '${type.simpleName}'")

    private fun <T : Any> createInstance(service: KClass<T>): T {
        val constructor = getPrimaryConstructor(service)
        val dependencies = constructor.parameters
            .map { parameter -> parameter.type.classifier }
            .filterIsInstance<KClass<*>>()
            .map(this::resolveDependency)
            .toTypedArray()
        return constructor.call(*dependencies)
    }

    private fun resolveDependency(dependency: KClass<*>): Any {
        if (dependency == Provider::class) {
            val provider = getPrimaryConstructor(dependency).call(this)
            return Provider::class.safeCast(provider)
                ?: throw IllegalArgumentException("lmao wtf...")
        }

        val data = services[dependency]
            ?: throw IllegalArgumentException("'${dependency.simpleName}' was not registered")

        return when (data.lifetime) {
            Lifetime.SINGLETON -> singletons.getOrPut(dependency) { createInstance(dependency) }
            Lifetime.TRANSIENT -> createInstance(dependency)
        }
    }

    fun <T : Any> get(service: KClass<T>): T {
        val instance = resolveDependency(service)
        return service.safeCast(instance)
            ?: throw IllegalArgumentException(
                "Resolved instance '${instance::class.simpleName}' does not implement '${service.simpleName}"
            )
    }

    inline fun <reified T : Any> get(): T =
        get(T::class)
}

private enum class Lifetime {
    SINGLETON,
    TRANSIENT
}

private data class ServiceData(
    val implementation: KClass<*>,
    val lifetime: Lifetime
)

/*
how is this instantiated?

val container = container {
    package("asd")
    package("kfe")
    services(
        UserService::class,
        UserController::class
    )
}

 */
