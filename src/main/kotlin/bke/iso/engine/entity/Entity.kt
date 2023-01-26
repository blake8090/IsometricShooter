package bke.iso.engine.entity

import bke.iso.engine.math.Location
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Entity(
    val id: UUID,
    private val callback: EntityService.Callback
) {

    var name: String = ""

    var x: Float = 0f
        set(value) {
            callback.onPositionChanged(this, value, y, z)
            field = value
        }

    var y: Float = 0f
        set(value) {
            callback.onPositionChanged(this, x, value, z)
            field = value
        }

    var z: Float = 0f
        set(value) {
            callback.onPositionChanged(this, x, y, value)
            field = value
        }

    var deleted = false
        private set

    private val components = mutableMapOf<KClass<out Component>, Component>()

    fun <T : Component> add(vararg components: T): Entity {
        for (component in components) {
            this.components[component::class] = component
            callback.onComponentAdded(this, component::class)
        }
        return this
    }

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        get(T::class)

    fun <T : Component> remove(type: KClass<T>) {
        components.remove(type)
        callback.onComponentRemoved(this, type)
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
        deleted = true
    }

    fun getLocation() =
        Location(x, y, z)
}
