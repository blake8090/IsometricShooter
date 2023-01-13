package bke.iso.service

import bke.iso.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.*

class ServiceContainer(classes: Set<KClass<*>>) {

    private val services = mutableMapOf<KClass<*>, Service<*>>()
    private val instances = mutableMapOf<KClass<*>, Any>()

    init {
        classes.forEach { kClass ->
            services[kClass] = createService(kClass)
        }
        validate()
        services.values.forEach { service -> initialize(service) }
    }

    /**
     * Returns an instance of the given [kClass].
     *
     * @throws [NoServiceFoundException] if no service found for [kClass]
     */
    fun <T : Any> get(kClass: KClass<T>): T {
        val service = services[kClass]
            ?: throw IllegalArgumentException("No service found for class ${kClass.simpleName}")

        val instance = when (service.lifetime) {
            Lifetime.SINGLETON -> instances[service.implClass]
                ?: throw RuntimeException("Expected instance for singleton ${kClass.simpleName}")

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
     */
    fun <T : Any> getProvider(kClass: KClass<T>): Provider<T> =
        Provider(this, kClass)

    /**
     * Returns a [Provider] of class [T].
     */
    inline fun <reified T : Any> getProvider() =
        getProvider(T::class)

    private fun <T : Any> createService(kClass: KClass<T>): Service<T> {
        val lifetime = getLifetime(kClass)

        val dependencies = kClass.primaryConstructor!!
            .parameters
            .filter { kParameter -> kParameter.kind == KParameter.Kind.VALUE }
            .map { kParameter -> kParameter.type }

        log.debug("Created $lifetime service '${kClass.simpleName}' with ${dependencies.size} dependencies")
        return Service(kClass, lifetime, dependencies)
    }

    private fun <T : Any> getLifetime(kClass: KClass<T>): Lifetime {
        val annotations = mutableSetOf<Annotation>()
        kClass.annotations.forEach(annotations::add)
        kClass.superclasses
            .flatMap(KClass<*>::annotations)
            .forEach(annotations::add)

        for (annotation in annotations) {
            if (annotation is Singleton) {
                return Lifetime.SINGLETON
            } else if (annotation is Transient) {
                return Lifetime.TRANSIENT
            }
        }

        throw MissingAnnotationsException("No annotations found for class ${kClass.simpleName}")
    }

    private fun validate() {
        for ((kClass, _) in services) {
            traverse(kClass, kClass, mutableListOf())
        }
    }

    private fun traverse(kClass: KClass<*>, head: KClass<*>, chain: MutableList<KClass<*>>) {
        if (kClass == Provider::class) {
            return
        } else if (chain.contains(head)) {
            val names = chain.map { it.simpleName }.joinToString()
            throw CircularDependencyException("Found circular dependency: ${head.simpleName} -> $names")
        }

        val service = services[kClass]!!
        for (dependency in service.dependencies) {
            val dependencyClass = dependency.classifier as KClass<*>
            chain.add(dependencyClass)
            traverse(dependencyClass, head, chain)
        }
    }

    private fun <T : Any> initialize(service: Service<T>) {
        for (dependency in service.dependencies) {
            val dependencyClass = dependency.classifier as KClass<*>
            if (dependencyClass == Provider::class) {
                // providers will be used at run time, so no initialization necessary here
                continue
            }
            val dependencyService = services[dependencyClass]
                ?: throw NoServiceFoundException("No service found for class ${dependencyClass.simpleName}")
            initialize(dependencyService)
        }

        if (service.lifetime == Lifetime.SINGLETON && !instances.containsKey(service.implClass)) {
            instances[service.implClass] = createInstance(service)
        }
    }

    private fun <T : Any> createInstance(service: Service<T>): T {
        val dependencies = service.dependencies
            .map(this::resolveDependency)
            .toTypedArray()
//        try {
            return if (dependencies.isEmpty()) {
                service.implClass.createInstance()
            } else {
                service.implClass.primaryConstructor!!.call(*dependencies)
            }
//        } catch (e: Exception) {
//            throw ServiceCreationException("Couldn't create instance of ${service.implClass.simpleName}: ${e.m}")
//        }
    }

    private fun resolveDependency(dependency: KType): Any {
        val kClass = dependency.classifier as KClass<*>
        return if (dependency.classifier == Provider::class) {
            val projectedClass = dependency.arguments
                .first()
                .type!!
                .classifier as KClass<*>
            getProvider(projectedClass)
        } else {
            get(kClass)
        }
    }
}

private data class Service<T : Any>(
    val implClass: KClass<out T>,
    val lifetime: Lifetime,
    val dependencies: List<KType>
)

private enum class Lifetime {
    SINGLETON,
    TRANSIENT
}
