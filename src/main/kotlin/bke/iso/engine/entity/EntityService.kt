package bke.iso.engine.entity

import bke.iso.app.service.Service
import bke.iso.engine.Location
import bke.iso.engine.log
import java.util.*
import kotlin.reflect.KClass

@Service
class EntityService {
    private val entityById = mutableMapOf<UUID, Entity>()
    private val deletedIds = mutableSetOf<UUID>()
    private val idsByComponent = mutableMapOf<KClass<out Component>, MutableSet<UUID>>()
    private val idsByLocation = mutableMapOf<Location, MutableSet<UUID>>()
        .toSortedMap(
            compareByDescending(Location::y)
                .thenBy(Location::x)
        )

    val search = EntitySearch(entityById, idsByComponent, idsByLocation)

    fun create(x: Float = 0f, y: Float = 0f): Entity {
        val id = UUID.randomUUID()
        val entity = Entity(id, Callback())
        entityById[id] = entity
        entity.x = x
        entity.y = y
        return entity
    }

    fun create(location: Location) =
        create(location.x.toFloat(), location.y.toFloat())

    fun get(id: UUID): Entity? =
        entityById[id]

    /**
     * Returns all entities, sorted by each entity's Y position, then X position
     */
    fun getAll(): List<Entity> =
        idsByLocation
            .flatMap { (_, ids) -> ids }
            .mapNotNull(this::get)

    private fun delete(id: UUID) {
        val entity = get(id) ?: return
        entity.removeAll()
        idsByLocation[Location(entity.x, entity.y)]?.remove(id)
        entityById.remove(id)
    }

    private fun <T : Component> onAddComponent(id: UUID, type: KClass<T>) =
        idsByComponent.getOrPut(type) { mutableSetOf() }.add(id)

    private fun <T : Component> onRemoveComponent(id: UUID, type: KClass<T>) =
        idsByComponent[type]?.remove(id)

    private fun onEntityDeleted(id: UUID) =
        deletedIds.add(id)

    private fun onPositionChanged(id: UUID, x: Float, y: Float) {
        val entity = get(id) ?: return
        val currentLocation = Location(entity.x, entity.y)
        val newLocation = Location(x, y)
        if (newLocation != currentLocation) {
            idsByLocation[currentLocation]?.remove(id)
            log.trace("Moved entity '$id' from '$currentLocation' to '$newLocation'")
        }
        idsByLocation.getOrPut(newLocation) { mutableSetOf() }.add(id)
    }

    inner class Callback {
        fun <T : Component> componentAdded(id: UUID, type: KClass<T>) =
            onAddComponent(id, type)

        fun <T : Component> componentRemoved(id: UUID, type: KClass<T>) =
            onRemoveComponent(id, type)

        fun entityDeleted(id: UUID) =
            onEntityDeleted(id)

        fun positionChanged(id: UUID, x: Float, y: Float) {
            onPositionChanged(id, x, y)
        }
    }
}
