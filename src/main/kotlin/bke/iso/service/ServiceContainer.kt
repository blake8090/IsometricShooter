package bke.iso.service

import bke.iso.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

class ServiceContainer(classes: Set<KClass<*>>) {
    private val cache = ServiceCache()

    init {
        classes.forEach { kClass -> register(kClass) }
        cache.initialize()
    }

    /**
     * Returns an instance of the given [kClass].
     *
     * @throws [NoServiceFoundException] if no service found for [kClass]
     */
    fun <T : Any> get(kClass: KClass<T>): T {
        return cache[kClass]
    }

    /**
     * Returns an instance of the given class [T].
     *
     * @throws [NoServiceFoundException] if no service found for class [T]
     */
    inline fun <reified T : Any> get() =
        get(T::class)

    private fun <T : Any> register(kClass: KClass<T>) {
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

        cache[kClass] = ServiceRecord(kClass, kClass, lifetime, dependencies)
        log.debug("Created $lifetime service '${kClass.simpleName}' with ${dependencies.size} dependencies")
    }
}
