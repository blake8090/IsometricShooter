package bke.iso.engine.entity

import bke.iso.engine.log
import bke.iso.engine.math.Location
import bke.iso.service.Singleton
import java.util.UUID
import kotlin.reflect.KClass

@Singleton
class EntityService {

    private val entities = mutableMapOf<UUID, Entity>()
    private val entitiesByComponent = mutableMapOf<KClass<out Component>, MutableSet<Entity>>()

    private val entityLocations = mutableMapOf<Location, MutableSet<Entity>>()
    private val entityLayers = mutableMapOf<Int, MutableSet<Entity>>()

    val search = EntitySearch(entitiesByComponent, entityLocations)

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

    fun layerCount() =
        entityLayers.filterValues { layer -> layer.isNotEmpty() }
            .keys
            .max()

    /**
     * Returns a list of all entities in the given layer (z-axis).
     *
     * Entities are sorted by their y pos (top to bottom), and then by their x pos (left to right)
     */
    fun getAllInLayer(z: Int) =
        entityLayers[z]
            ?.sortedWith(
                // TODO: fix sorting order - use Location instead of raw Entity positions?
                compareByDescending(Entity::y)
                    .thenBy(Entity::x)
            )
            ?: emptyList()

    fun getAll() =
        entities.values.toList()

    fun update() {
        val deletedEntries = entities.filterValues(Entity::deleted)
        for ((id, entity) in deletedEntries) {
            entity.removeAll()
            val location = entity.getLocation()
            entityLocations[location]?.remove(entity)
            entityLayers[location.z]?.remove(entity)
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
            val currentLocation = entity.getLocation()
            val newLocation = Location(x, y, z)
            // TODO: check for x, y, and z changes separately
            if (currentLocation != newLocation) {
                entityLocations[currentLocation]?.remove(entity)
                entityLayers[currentLocation.z]?.remove(entity)
                log.trace("Moved entity '${entity.id}' from '$currentLocation' to '$newLocation'")
            }
            entityLocations
                .getOrPut(newLocation) { mutableSetOf() }
                .add(entity)
            entityLayers
                .getOrPut(newLocation.z) { mutableSetOf() }
                .add(entity)
        }
    }
}
