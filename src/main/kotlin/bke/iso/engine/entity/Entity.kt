package bke.iso.engine.entity

import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class Entity(
    val id: UUID,
    private val callback: EntityService.Callback
) {
    var name: String = ""

    var x: Float = 0f
        set(value) {
            callback.positionChanged(id, value, y)
            field = value
        }

    var y: Float = 0f
        set(value) {
            callback.positionChanged(id, x, value)
            field = value
        }

    var deleted = false
        private set

    private val components = mutableMapOf<KClass<out Component>, Component>()

    fun <T : Component> add(vararg components: T) {
        for (component in components) {
            this.components[component::class] = component
            callback.componentAdded(id, component::class)
        }
    }

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        get(T::class)

    fun <T : Component> remove(type: KClass<T>) {
        components.remove(type)
        callback.componentRemoved(id, type)
    }

    inline fun <reified T : Component> remove() =
        remove(T::class)

    fun removeAll() =
        components.keys
            .toList()
            .forEach { type -> remove(type) }

    fun <T : Component> has(type: KClass<T>) =
        components.contains(type)

    inline fun <reified T : Component> has() =
        has(T::class)

    fun delete() {
        callback.entityDeleted(id)
        deleted = true
    }

    override fun equals(other: Any?): Boolean {
        return (other is Entity) && other.id == id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + components.hashCode()
        return result
    }
}
