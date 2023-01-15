package bke.iso.service

import bke.iso.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.cast
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

class ServiceContainer(classes: Set<KClass<*>>) {
    private val records = mutableMapOf<KClass<*>, ServiceRecord<*>>()
    private val instances = mutableMapOf<KClass<*>, ServiceInstance<*>>()

    init {
        classes.forEach { kClass ->
            records[kClass] = register(kClass)
        }

        for (record in records.values) {
            validate(record, mutableSetOf())
        }

        records.values.forEach(this::initialize)
    }

    /**
     * Returns an instance of the given [kClass].
     *
     * @throws [NoServiceFoundException] if no service found for [kClass]
     */
    fun <T : Any> get(kClass: KClass<T>): T {
        val serviceRecord = records[kClass]
            ?: throw NoServiceFoundException(kClass)
        val serviceInstance = findServiceInstance(serviceRecord)
        return kClass.cast(serviceInstance.instance)
    }

    /**
     * Returns an instance of the given class [T].
     *
     * @throws [NoServiceFoundException] if no service found for class [T]
     */
    inline fun <reified T : Any> get() =
        get(T::class)

    private fun findServiceInstance(record: ServiceRecord<*>): ServiceInstance<*> {
        return when (record.lifetime) {
            Lifetime.SINGLETON -> {
                val implClass = record.implClass
                instances[implClass]
                    ?: throw Error(
                        "Expected instance of class {${implClass.simpleName} "
                                + "for service ${record.baseClass.simpleName}"
                    )
            }

            Lifetime.TRANSIENT -> createServiceInstance(record)
        }
    }

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

    private fun <T : Any> register(kClass: KClass<T>): ServiceRecord<T> {
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
            .filter { kParameter -> kParameter.kind == KParameter.Kind.VALUE }
            .map { kParameter -> kParameter.type }

        log.debug("Created $lifetime service '${kClass.simpleName}' with ${dependencies.size} dependencies")
        return ServiceRecord(kClass, kClass, lifetime, dependencies)
    }

    private fun validate(record: ServiceRecord<*>, visitedRecords: MutableSet<ServiceRecord<*>>) {
        if (!visitedRecords.add(record)) {
            val names = visitedRecords
                .map { it.baseClass.simpleName }
                .joinToString()
            throw CircularDependencyException("Found circular dependency: $names -> ${record.baseClass.simpleName}")
        }

        val serviceDependencies = record.dependencies
            .map { kType -> kType.classifier as KClass<*> }
            .filter { kClass -> kClass != Provider::class }

        for (dependency in serviceDependencies) {
            val dependencyRecord = records[dependency]
                ?: throw NoServiceFoundException(dependency)
            validate(dependencyRecord, visitedRecords)
        }
    }

    private fun initialize(record: ServiceRecord<*>) {
        if (record.initialized) {
            return
        } else if (record.lifetime == Lifetime.SINGLETON) {
            val serviceInstance = createServiceInstance(record)
            // PostInit should be called in order of initialization - therefore, call the dependencies first
            // TODO: add unit test to verify this behavior?
            serviceInstance.dependencies.forEach(this::callPostInit)
            callPostInit(serviceInstance)
            instances[record.implClass] = serviceInstance
        }
        record.initialized = true
    }

    private fun createServiceInstance(record: ServiceRecord<*>): ServiceInstance<*> {
        val serviceInstances = mutableListOf<ServiceInstance<*>>()
        val parameters = record.dependencies.map { dependency ->
            val kClass = dependency.classifier as KClass<*>
            if (kClass == Provider::class) {
                createProviderFromType(dependency)
            } else {
                val serviceInstance = resolveServiceDependency(kClass)
                serviceInstances.add(serviceInstance)
                serviceInstance.instance
            }
        }

        val kClass = record.implClass
        val constructor = kClass.primaryConstructor!!
        val instance = constructor.call(*parameters.toTypedArray())
        return ServiceInstance(instance, serviceInstances)
    }

    private fun createProviderFromType(kType: KType): Provider<*> {
        val typeParameter = kType.arguments.first().type!!
        return getProvider(typeParameter.classifier as KClass<*>)
    }

    private fun resolveServiceDependency(kClass: KClass<*>): ServiceInstance<*> {
        val dependencyRecord = records[kClass] ?: throw NoServiceFoundException(kClass)
        if (!dependencyRecord.initialized) {
            initialize(dependencyRecord)
        }
        return findServiceInstance(dependencyRecord)
    }

    private fun callPostInit(instance: ServiceInstance<*>) {
        if (instance.postInitComplete || instance.instance::class == Provider::class) {
            return
        }

        instance.dependencies.forEach(this::callPostInit)
        // TODO: validate that only one function has PostInit annotation?
        instance::class.functions
            .firstOrNull { func -> func.hasAnnotation<PostInit>() }
            ?.call(instance.instance)
        instance.postInitComplete = true
    }
}

private enum class Lifetime {
    SINGLETON,
    TRANSIENT
}

private data class ServiceRecord<T : Any>(
    val baseClass: KClass<T>,
    val implClass: KClass<out T>,
    val lifetime: Lifetime,
    val dependencies: List<KType>,
    var initialized: Boolean = false
)

private data class ServiceInstance<T : Any>(
    val instance: T,
    val dependencies: List<ServiceInstance<*>>,
    var postInitComplete: Boolean = false
)
