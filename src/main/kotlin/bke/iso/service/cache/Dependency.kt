package bke.iso.service.cache

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
    private val cache: Cache,
    private val baseClass: KClass<T>
) : Dependency<NewProvider<T>> {

    override fun getInstance(): NewProvider<T> {
        return NewProvider(cache, baseClass)
    }
}

class NewProvider<T : Any>(
    private val cache: Cache,
    private val baseClass: KClass<T>
) {

    fun get(): T =
        cache[baseClass]

    fun <U : T> get(kClass: KClass<U>): U =
        cache[kClass]
}
