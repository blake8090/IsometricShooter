package bke.iso.engine.world.entity

import bke.iso.engine.util.getLogger
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class EntityDatabase {
    private val log = getLogger()

    private val entityIds = mutableSetOf<Int>()
    private val componentByEntityId = mutableMapOf<Pair<Int, KClass<out Component>>, Component>()
    private val entityIdsByComponent = mutableMapOf<KClass<out Component>, MutableSet<Int>>()
    private var latestEntityId: Int = 0

    // todo: setup() - search components by subtype. any name collisions should throw an exception

    fun createEntity(): Int {
        val id = latestEntityId + 1
        if (!entityIds.add(id)) {
            throw IllegalArgumentException("already exists")
        }
        latestEntityId = id
        return id
    }

    fun contains(id: Int) = entityIds.contains(id)

    fun <T : Component> findComponent(id: Int, type: KClass<T>): T? {
        val instance = componentByEntityId[Pair(id, type)]
        return type.safeCast(instance)
    }

    inline fun <reified T : Component> findComponent(id: Int): T? =
        findComponent(id, T::class)

    fun <T : Component> setComponent(id: Int, component: T) {
        if (!entityIds.contains(id)) {
            log.warn("Entity $id does not exist")
            return
        }
        componentByEntityId[Pair(id, component::class)] = component
        entityIdsByComponent.getOrPut(component::class) { mutableSetOf() }.add(id)
    }

    fun <T : Component> findEntitiesWithComponent(componentType: KClass<out T>): Set<Int> =
        entityIdsByComponent[componentType] ?: emptySet()

    inline fun <reified T : Component> findEntitiesWithComponent(): Set<Int> =
        findEntitiesWithComponent(T::class)
}
