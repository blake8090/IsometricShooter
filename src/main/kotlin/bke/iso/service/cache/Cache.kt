package bke.iso.service.cache

import bke.iso.service.*
import bke.iso.service.Lifetime
import kotlin.reflect.*
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class Cache {

    private val records = mutableMapOf<KClass<*>, Record<*>>()

    fun init(classes: Set<KClass<*>>) {
        for (kClass in classes) {
            createRecord(kClass, mutableSetOf())
        }
        records.values.forEach(Record<*>::init)
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

        val dependencyTypes = kClass.primaryConstructor!!
            .parameters
            .filter { kParameter -> kParameter.kind == KParameter.Kind.VALUE }
            .map { kParameter -> kParameter.type }

        val dependencies = mutableSetOf<Dependency<*>>()
        for (kType in dependencyTypes) {
            dependencies.add(createDependency(kType, chain))
        }

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
        return if (kClass == NewProvider::class) {
            val typeParameter = kType.arguments.first().type!!
            ProviderDependency(this, typeParameter.jvmErasure)
        } else {
            val subChain = chain.toMutableSet()
            RecordDependency(createRecord(kClass, subChain))
        }
    }
}
