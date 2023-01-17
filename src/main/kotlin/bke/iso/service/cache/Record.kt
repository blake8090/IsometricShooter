package bke.iso.service.cache

import bke.iso.service.MissingInstanceException
import bke.iso.service.ServiceCreationException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

internal enum class Lifetime {
    SINGLETON,
    TRANSIENT
}

internal data class Record<T : Any>(
    val kClass: KClass<T>,
    val lifetime: Lifetime,
    val dependencies: Set<Dependency<*>>
) {

    private var instance: Instance<out T>? = null

    fun init() {
        if (lifetime == Lifetime.SINGLETON) {
            instance = createInstance()
        }
    }

    fun postInit() {
        instance?.callPostInit()
    }

    fun getInstance(): Instance<out T> =
        when (lifetime) {
            Lifetime.SINGLETON -> instance ?: throw MissingInstanceException(kClass, kClass)
            Lifetime.TRANSIENT -> {
                val instance = createInstance()
                instance.callPostInit()
                instance
            }
        }

    private fun createInstance(): Instance<out T> {
        val params = dependencies.map { dependency -> dependency.getInstance() }
        try {
            val value =
                if (params.isEmpty()) {
                    kClass.createInstance()
                } else {
                    kClass.primaryConstructor!!.call(*params.toTypedArray())
                }
            return Instance(value)
        } catch (e: Exception) {
            throw ServiceCreationException("Error creating instance of service ${kClass.simpleName}:", e)
        }
    }
}
