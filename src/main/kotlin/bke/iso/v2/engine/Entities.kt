package bke.iso.v2.engine

import bke.iso.engine.world.entity.Component
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class Entities {
    private val components = mutableMapOf<UUID, ComponentMap>()

    fun createEntity(): UUID {
        val id = UUID.randomUUID()
        components[id] = ComponentMap()
        // todo: default all entities to position 0,0
        return id
    }

    fun <T : Component> getComponent(id: UUID, type: KClass<T>): T? {
        val map = components[id] ?: return null
        return type.safeCast(map.components[type])
    }

    fun <T : Component> setComponent(id: UUID, component: T) =
        components[id]?.set(component)
}

private class ComponentMap {
    val components = mutableMapOf<KClass<out Component>, Component>()

    inline fun <reified T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    fun <T : Component> set(component: T) {
        components[component::class] = component
    }
}
