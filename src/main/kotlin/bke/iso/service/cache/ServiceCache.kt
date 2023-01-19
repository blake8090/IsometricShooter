package bke.iso.service.cache

import bke.iso.service.CircularDependencyException
import bke.iso.service.MissingAnnotationsException
import bke.iso.service.NoServiceFoundException
import bke.iso.service.Provider
import bke.iso.service.Singleton
import bke.iso.service.Transient
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.cast
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

// TODO: rename to container, fix up API
class ServiceCache {

    private val records = mutableMapOf<KClass<*>, Record<*>>()

    fun init(classes: Set<KClass<*>>) {
        classes.forEach { kClass -> createRecord(kClass, mutableSetOf()) }
        records.values.forEach(Record<*>::init)
        records.values.forEach(Record<*>::postInit)
    }

    operator fun <T : Any> get(kClass: KClass<T>): T {
        val record = records[kClass] ?: throw NoServiceFoundException(kClass)
        val instance = record.getInstance().value
        return kClass.cast(instance)
    }

    private fun <T : Any> createRecord(kClass: KClass<T>, chain: MutableSet<KClass<*>>): Record<*> {
        if (!chain.add(kClass)) {
            val names = chain.map { it.simpleName }.joinToString(" -> ")
            val message = "Found circular dependency: $names -> ${kClass.simpleName}"
            throw CircularDependencyException(message)
        }

        if (records.containsKey(kClass)) {
            return records[kClass]!!
        }

        val dependencies = kClass.primaryConstructor!!
            .parameters
            .filter { param -> param.kind == KParameter.Kind.VALUE }
            .map { param -> createDependency(param.type, chain) }
            .toSet()

        val record = Record(kClass, getLifetime(kClass), dependencies)
        records[kClass] = record
        return record
    }

    private fun getLifetime(kClass: KClass<*>): Lifetime =
        if (kClass.hasAnnotation<Singleton>()) {
            Lifetime.SINGLETON
        } else if (kClass.hasAnnotation<Transient>()) {
            Lifetime.TRANSIENT
        } else {
            throw MissingAnnotationsException("No annotations found for class ${kClass.simpleName}")
        }

    private fun createDependency(kType: KType, chain: MutableSet<KClass<*>>): Dependency<*> {
        val kClass = kType.jvmErasure
        return if (kClass == Provider::class) {
            val typeParameter = kType.arguments.first().type!!
            ProviderDependency(this, typeParameter.jvmErasure)
        } else {
            val subChain = chain.toMutableSet()
            ServiceDependency(createRecord(kClass, subChain))
        }
    }
}
