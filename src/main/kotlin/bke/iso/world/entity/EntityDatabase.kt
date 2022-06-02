package bke.iso.world.entity

import bke.iso.util.getLogger
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class Component

data class PositionComponent(
    var x: Float = 0f,
    var y: Float = 0f
) : Component()

data class TextureComponent(var name: String) : Component()

class EntityDatabase {
    private val log = getLogger(this)

    private val entityIds = mutableSetOf<Int>()
    private val componentByEntityId = mutableMapOf<Pair<Int, KClass<out Component>>, Component>()
    private var latestEntityId: Int = 0

    // todo: setup() - search components by subtype. any name collisions should throw an exception

    fun createEntity(): Int? {
        val id = latestEntityId + 1
        if (!entityIds.add(id)) {
            log.warn("Entity with id $id already exists")
            return null
        }
        latestEntityId = id
        return id
    }

    fun contains(id: Int) = entityIds.contains(id)

    fun <T : Component> getComponent(id: Int, type: KClass<T>): T? {
        val instance = componentByEntityId[Pair(id, type)]
        return type.safeCast(instance)
    }

    inline fun <reified T : Component> getComponent(id: Int): T? {
        return getComponent(id, T::class)
    }

    fun <T : Component> setComponent(id: Int, component: T) {
        if (!entityIds.contains(id)) {
            log.warn("Entity $id does not exist")
            return
        }
        componentByEntityId[Pair(id, component::class)] = component
    }
}
