package bke.iso.v2.engine

import java.lang.IllegalArgumentException
import java.util.UUID
import kotlin.reflect.KClass

class Entities {
    private val components = mutableMapOf<UUID, ComponentMap>()
    private val deletedIds = mutableListOf<UUID>()

    fun create(): UUID {
        val id = UUID.randomUUID()
        setPos(id, 0f, 0f)
        return id
    }

    fun delete(id: UUID) {
        deletedIds.add(id)
    }

    fun <T : Component> getComponent(id: UUID, type: KClass<T>): T? {
        return components.getOrPut(id) { ComponentMap() }
            .get(type)
    }

    fun <T : Component> setComponent(id: UUID, component: T) {
        components.getOrPut(id) { ComponentMap() }
            .set(component)
    }

    fun <T : Component> hasComponent(id: UUID, type: KClass<T>): Boolean {
        return components.getOrPut(id) { ComponentMap() }
            .has(type)
    }

    fun setPos(id: UUID, x: Float, y: Float) {
        setComponent(id, Position(x, y))
    }

    fun move(id: UUID, dx: Float, dy: Float) {
        val pos = getComponent(id, Position::class) ?: return
        pos.x += dx
        pos.y += dy
    }

    fun update() {
        deletedIds.forEach { id ->
            components.remove(id)
        }
    }
}

private class ComponentMap {
    val components = mutableMapOf<KClass<out Component>, Component>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> get(type: KClass<T>): T? {
        val component = components[type] ?: return null
        if (!type.isInstance(component)) {
            throw IllegalArgumentException()
        }
        // todo: utility function for cast with appropriate exceptions if not an instance
        return component as T
    }

    fun <T : Component> set(component: T) {
        components[component::class] = component
    }

    fun <T : Component> has(type: KClass<T>): Boolean {
        return components.containsKey(type)
    }
}
