package bke.iso.engine.entity

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class Component

class EntityComponents {
    private val components = mutableMapOf<Pair<UUID, KClass<out Component>>, Component>()
    private val idsByType = mutableMapOf<KClass<out Component>, MutableSet<UUID>>()

    operator fun <T : Component> get(id: UUID, type: KClass<T>): T? {
        val component = components[id to type]
        return type.safeCast(component)
    }

    operator fun <T : Component> set(id: UUID, component: T) {
        components[id to component::class] = component
        idsByType
            .getOrPut(component::class) { mutableSetOf() }
            .add(id)
    }

    operator fun <T : Component> contains(idToType: Pair<UUID, KClass<T>>): Boolean =
        components.containsKey(idToType)

    fun <T : Component> remove(id: UUID, type: KClass<T>) {
        components.remove(id to type)
        idsByType[type]?.remove(id)
    }

    fun <T : Component> removeAll(type: KClass<T>) {
        idsByType[type]?.forEach { id ->
            remove(id, type)
        }
        idsByType.remove(type)
    }

    fun <T : Component> getIdsWith(type: KClass<T>): Set<UUID> =
        idsByType[type]
            ?.toSet()
            ?: emptySet()
}
