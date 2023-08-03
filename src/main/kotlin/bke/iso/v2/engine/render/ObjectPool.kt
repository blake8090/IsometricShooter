package bke.iso.v2.engine.render

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pool.Poolable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

class ObjectPool<T : Poolable>(private val type: KClass<T>) {

    private val instances = mutableSetOf<T>()
    private val pool = object : Pool<T>() {
        override fun newObject(): T =
            type.createInstance()
    }

    init {
        requireNotNull(type.primaryConstructor) {
            "Need a primary constructor"
        }
    }

    operator fun iterator() =
        instances.iterator()

    fun obtain(): T {
        val instance = pool.obtain()
        instances.add(instance)
        return instance
    }

    fun getAll(): Set<T> =
        instances

    fun clear() {
        instances.forEach(pool::free)
        instances.clear()
    }

    companion object {
        inline fun <reified T : Poolable> new(): ObjectPool<T> =
            ObjectPool(T::class)
    }
}
