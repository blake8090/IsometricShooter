package bke.iso.engine.entity

import bke.iso.engine.log
import bke.iso.engine.math.Location
import java.util.UUID

class EntityLocations {

    private val ids = mutableSetOf<UUID>()
    private val layers = mutableMapOf<Int, LocationMap>()

    fun add(entity: Entity) {
        add(entity, entity.getLocation())
    }

    private fun add(entity: Entity, location: Location) {
        layers.getOrPut(location.z) { mutableMapOf() }
            .getOrPut(location) { mutableSetOf() }
            .add(entity)
        ids.add(entity.id)
    }

    fun remove(entity: Entity) {
        val location = entity.getLocation()
        val locationMap = layers[location.z] ?: return
        locationMap[location]?.remove(entity)
        ids.remove(entity.id)
    }

    fun update(entity: Entity, x: Float, y: Float, z: Float) {
        val newLocation = Location(x, y, z)
        if (!ids.contains(entity.id)) {
            add(entity, newLocation)
            return
        }

        val currentLocation = entity.getLocation()
        if (currentLocation != newLocation) {
            remove(entity)
            add(entity, newLocation)
            log.trace("Moved entity '${entity.id}' from '$currentLocation' to '$newLocation'")
        }
    }

    fun layerCount() =
        layers.filterValues { layer -> layer.isNotEmpty() }
            .keys
            .max()

    fun getAllInLayer(z: Int): List<Entity> {
        val locationMap = layers[z] ?: return emptyList()
        return locationMap
            .toSortedMap(
                compareByDescending(Location::y)
                    .thenBy(Location::x)
            )
            .flatMap { (_, entities) -> entities }
    }

    fun getAllAtLocation(x: Int, y: Int, z: Int): List<Entity> {
        val location = Location(x, y, z)
        val locationMap = layers[location.z] ?: return emptyList()
        return locationMap[location]
            ?.toList()
            ?: emptyList()
    }
}

private typealias LocationMap = MutableMap<Location, MutableSet<Entity>>
