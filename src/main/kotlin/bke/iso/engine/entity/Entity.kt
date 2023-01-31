package bke.iso.engine.entity

import bke.iso.engine.world.WorldObject
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Entity(val id: UUID) : WorldObject() {

    override val layer = 1

    private val components = mutableMapOf<KClass<out Component>, Component>()

    fun <T : Component> add(vararg components: T): Entity {
        for (component in components) {
            this.components[component::class] = component
        }
        return this
    }

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        get(T::class)

    fun <T : Component> remove(type: KClass<T>) {
        components.remove(type)
    }

    inline fun <reified T : Component> remove() =
        remove(T::class)

    fun <T : Component> has(type: KClass<T>) =
        components.contains(type)

    inline fun <reified T : Component> has() =
        has(T::class)
}
