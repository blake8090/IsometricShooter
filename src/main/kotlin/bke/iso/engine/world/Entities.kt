package bke.iso.engine.world

import bke.iso.engine.log
import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class Entities(private val grid: WorldGrid) {
    private val posById = mutableMapOf<UUID, Vector2>()
    private val componentsById = mutableMapOf<UUID, ComponentMap>()

    fun create(): UUID {
        val id = UUID.randomUUID()
        setPos(id, 0f, 0f)
        return id
    }

    /**
     * If the entity ID exists, returns the entity's precise position.
     */
    fun getPos(id: UUID): Vector2? =
        posById[id]

    fun setPos(id: UUID, x: Float, y: Float) {
        posById[id] = Vector2(x, y)

        val location = Location(x.toInt(), y.toInt())
        grid.setEntityLocation(id, location)
    }

    fun move(id: UUID, dx: Float, dy: Float) {
        val pos = posById[id] ?: return
        setPos(id, pos.x + dx, pos.y + dy)
    }

    // TODO: handle entity deletion

    fun <T : Component> addComponent(id: UUID, component: T) {
        if (!posById.containsKey(id)) {
            log.warn("Entity with id $id does not exist")
            return
        }
        componentsById
            .getOrPut(id) { ComponentMap() }
            .add(component)
    }

    fun <T : Component> getComponent(id: UUID, type: KClass<T>): T? {
        if (!posById.containsKey(id)) {
            log.warn("Entity with id $id does not exist")
            return null
        }
        return componentsById[id]?.get(type)
    }

    fun <T : Component> hasComponent(id: UUID, type: KClass<T>): Boolean =
        componentsById[id]
            ?.has(type)
            ?: false
}

private class ComponentMap {
    val components = mutableMapOf<KClass<out Component>, Component>()

    fun <T : Component> add(component: T) {
        components[component::class] = component
    }

    fun <T : Component> get(type: KClass<T>): T? {
        return type.safeCast(components[type])
    }

    fun <T : Component> has(type: KClass<T>): Boolean =
        components.containsKey(type)
}
