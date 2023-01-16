package bke.iso.service

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.cast
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

internal class ServiceCache {
    private val records = mutableMapOf<KClass<*>, ServiceRecord<*>>()
    private val instances = mutableMapOf<KClass<*>, ServiceInstance<*>>()

    operator fun <T : Any> set(kClass: KClass<T>, serviceRecord: ServiceRecord<T>) {
        // TODO: throw exception on duplicate
        records[kClass] = serviceRecord
    }

    operator fun <T : Any> get(kClass: KClass<T>): T {
        val record = records[kClass] ?: throw NoServiceFoundException(kClass)
        val instance = getServiceInstance(record).instance
        return kClass.cast(instance)
    }

    private fun getServiceInstance(record: ServiceRecord<*>): ServiceInstance<*> {
        return when (record.lifetime) {
            Lifetime.SINGLETON -> instances[record.implClass]
                ?: throw MissingInstanceException(record.baseClass, record.implClass)

            Lifetime.TRANSIENT -> createServiceInstance(record)
        }
    }

    fun initialize() {
        validate()
        records.values.forEach(this::initialize)
    }

    private fun validate() {
        for ((_, record) in records) {
            traverse(record, record, mutableSetOf())
        }
    }

    private fun traverse(record: ServiceRecord<*>, head: ServiceRecord<*>, visited: MutableSet<ServiceRecord<*>>) {
        if (!visited.add(record)) {
            val names = visited.map { it.baseClass.simpleName }.joinToString(" -> ")
            val message = "Found circular dependency: $names -> ${record.baseClass.simpleName}"
            throw CircularDependencyException(message)
        }

        val serviceDependencies = record.dependencies
            .map { kType -> kType.classifier as KClass<*> }
            .filter { kClass -> kClass != Provider::class }

        for (dependency in serviceDependencies) {
            val dependencyRecord = records[dependency] ?: throw NoServiceFoundException(dependency)
            val subChain = mutableSetOf<ServiceRecord<*>>()
            subChain.addAll(visited)
            traverse(dependencyRecord, head, subChain)
        }
    }

    private fun initialize(record: ServiceRecord<*>) {
        if (record.initialized) {
            return
        } else if (record.lifetime == Lifetime.SINGLETON) {
            val serviceInstance = createServiceInstance(record)
            // PostInit should be called in order of initialization - therefore, call the dependencies first
            // TODO: add unit test to verify this behavior?
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

        val constructor = record.implClass.primaryConstructor!!
        val instance = constructor.call(*parameters.toTypedArray())
        return ServiceInstance(instance, serviceInstances)
    }

    private fun createProviderFromType(kType: KType): Provider<*> {
        val typeParameter = kType.arguments.first().type!!
        return Provider(this, typeParameter.classifier as KClass<*>)
    }

    private fun resolveServiceDependency(kClass: KClass<*>): ServiceInstance<*> {
        val dependencyRecord = records[kClass] ?: throw NoServiceFoundException(kClass)
        if (!dependencyRecord.initialized) {
            initialize(dependencyRecord)
        }
        return getServiceInstance(dependencyRecord)
    }

    private fun callPostInit(instance: ServiceInstance<*>) {
        if (instance.postInitComplete || instance.instance::class == Provider::class) {
            return
        }

        // PostInit should be called in order of initialization - therefore, call the dependencies first
        // TODO: add unit test to verify this behavior?
        instance.dependencies.forEach(this::callPostInit)
        // TODO: validate that only one function has PostInit annotation?
        instance::class.functions
            .firstOrNull { func -> func.hasAnnotation<PostInit>() }
            ?.call(instance.instance)
        instance.postInitComplete = true
    }
}

internal enum class Lifetime {
    SINGLETON,
    TRANSIENT
}

internal data class ServiceRecord<T : Any>(
    val baseClass: KClass<T>,
    val implClass: KClass<out T>,
    val lifetime: Lifetime,
    val dependencies: List<KType>,
    var initialized: Boolean = false
)

internal data class ServiceInstance<T : Any>(
    val instance: T,
    val dependencies: List<ServiceInstance<*>>,
    var postInitComplete: Boolean = false
)
