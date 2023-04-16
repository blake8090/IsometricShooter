package bke.iso.service.container

import bke.iso.engine.log
import bke.iso.service.CircularDependencyException
import bke.iso.service.MissingAnnotationsException
import bke.iso.service.MissingInstanceException
import bke.iso.service.NoServiceFoundException
import bke.iso.service.Provider
import bke.iso.service.ServiceCreationException
import bke.iso.service.Singleton
import bke.iso.service.Transient
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class ServiceContainer {

    private val records = mutableMapOf<KClass<*>, ServiceRecord<*>>()
    private val instances = mutableMapOf<KClass<*>, ServiceInstance<*>>()

    fun init(classes: Set<KClass<*>>) {
        classes.forEach { kClass -> createRecord(kClass, mutableSetOf()) }
        records.values.forEach { record -> initRecord(record) }
        instances.values.forEach(ServiceInstance<*>::callPostInit)
    }

    fun <T : Any> get(kClass: KClass<T>): T {
        val serviceInstance = findInstance(kClass)
        return kClass.cast(serviceInstance.value)
    }

    inline fun <reified T : Any> get() =
        get(T::class)

    private fun getRecord(kClass: KClass<*>) =
        records[kClass] ?: throw NoServiceFoundException(kClass)

    private fun <T : Any> initRecord(record: ServiceRecord<T>) {
        if (record.lifetime == Lifetime.SINGLETON) {
            instances[record.kClass] = createInstance(record)
        }
        record.initialized = true
    }

    private fun <T : Any> createRecord(kClass: KClass<T>, chain: MutableSet<KClass<*>>) {
        if (!chain.add(kClass)) {
            val names = chain.map { it.simpleName }.joinToString(" -> ")
            val message = "Found circular dependency: $names -> ${kClass.simpleName}"
            throw CircularDependencyException(message)
        } else if (records.containsKey(kClass)) {
            return
        }

        val dependencies = kClass.primaryConstructor!!
            .parameters
            .filter { param -> param.kind == KParameter.Kind.VALUE }
            .map { param -> createDependency(param.type, chain) }

        val lifetime = getLifetime(kClass)
        val record = ServiceRecord(kClass, lifetime, dependencies)
        records[kClass] = record
        log.debug(
            "Created service record '${kClass.simpleName}'"
                    + " with lifetime '$lifetime'"
                    + " and '${dependencies.size}' dependencies"
        )
    }

    private fun getLifetime(kClass: KClass<*>): Lifetime =
        if (kClass.hasAnnotation<Singleton>()) {
            Lifetime.SINGLETON
        } else if (kClass.hasAnnotation<Transient>()) {
            Lifetime.TRANSIENT
        } else {
            throw MissingAnnotationsException("No annotations found for class ${kClass.simpleName}")
        }

    private fun createDependency(kType: KType, chain: MutableSet<KClass<*>>): () -> ServiceInstance<*> {
        val kClass = kType.jvmErasure

        if (kClass == Provider::class) {
            val parameterizedClass = kType.arguments
                .first()
                .type!!
                .jvmErasure
            return { ServiceInstance(Provider(this, parameterizedClass)) }
        } else {
            val subChain = chain.toMutableSet()
            createRecord(kClass, subChain)
            return { findInstance(kClass) }
        }
    }

    private fun findInstance(kClass: KClass<*>): ServiceInstance<*> {
        val record = getRecord(kClass)
        val instance =
            when (record.lifetime) {
                Lifetime.SINGLETON -> instances[kClass] ?: throw MissingInstanceException(kClass, kClass)
                Lifetime.TRANSIENT -> createInstance(record)
            }
        instance.callPostInit()
        return instance
    }

    private fun <T : Any> createInstance(record: ServiceRecord<T>): ServiceInstance<T> {
        val kClass = record.kClass
        val dependencies = record.dependencies.map { provider -> provider.invoke() }

        try {
            val params = dependencies
                .map { instance -> instance.value }
                .toTypedArray()

            val instance =
                if (params.isEmpty()) {
                    kClass.createInstance()
                } else {
                    kClass.primaryConstructor!!.call(*params)
                }

            return ServiceInstance(instance)
        } catch (e: Exception) {
            throw ServiceCreationException("Error creating instance of service ${kClass.simpleName}:", e)
        }
    }
}
