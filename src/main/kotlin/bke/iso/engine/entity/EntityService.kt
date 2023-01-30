package bke.iso.engine.entity

import bke.iso.engine.math.Location
import bke.iso.engine.world.WorldService
import bke.iso.service.Singleton
import java.util.UUID
import kotlin.reflect.KClass

@Singleton
class EntityService(private val worldService: WorldService) {

    private val entities = mutableMapOf<UUID, Entity>()
    private val entitiesByComponent = mutableMapOf<KClass<out Component>, MutableSet<Entity>>()

    val search = EntitySearch(entitiesByComponent)

    fun create(x: Float, y: Float, z: Float): Entity {
        val id = UUID.randomUUID()
        val entity = Entity(id, Callback())
        entities[id] = entity
        entity.x = x
        entity.y = y
        entity.z = z
        return entity
    }

    fun create(location: Location) =
        create(location.x.toFloat(), location.y.toFloat(), location.z.toFloat())

    fun update() {
        val deletedEntries = entities.filterValues(Entity::deleted)
        for ((id, entity) in deletedEntries) {
            entity.removeAll()
            worldService.removeEntity(entity)
            entities.remove(id)
        }
    }

    /**
     * Used only by the [Entity] class to inform the [EntityService] on any changes,
     * such as a component being added, position being changed, or entity being deleted.
     * This allows the [EntityService] to keep all Entity records up to date.
     */
    inner class Callback {

        fun <T : Component> onComponentAdded(entity: Entity, type: KClass<T>) {
            entitiesByComponent
                .getOrPut(type) { mutableSetOf() }
                .add(entity)
        }

        fun <T : Component> onComponentRemoved(entity: Entity, type: KClass<T>) {
            entitiesByComponent[type]?.remove(entity)
        }

        fun onPositionChanged(entity: Entity, x: Float, y: Float, z: Float) {
            worldService.updateEntity(entity, x, y, z)
        }
    }
}
