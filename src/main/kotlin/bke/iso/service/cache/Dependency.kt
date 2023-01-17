package bke.iso.service.cache

import bke.iso.service.Provider
import kotlin.reflect.KClass

internal interface Dependency<Any> {
    fun getInstance(): Any
}

internal class RecordDependency<T : Any>(private val record: Record<T>) : Dependency<T> {

    override fun getInstance(): T {
        return record.getInstance().value
    }
}

internal class ProviderDependency<T : Any>(
    private val cache: ServiceCache,
    private val baseClass: KClass<T>
) : Dependency<Provider<T>> {

    override fun getInstance(): Provider<T> {
        return Provider(cache, baseClass)
    }
}
