package bke.iso.v2.engine.world

import bke.iso.v2.engine.log
import java.util.*

/**
 * Represents a specific location on the world grid.
 */
data class Location(val x: Int, val y: Int)

class WorldGrid {
    /**
     * Maps an entity ID to a location.
     * Provides a quick reference to a particular location when entity positions are updated.
     */
    private val locationByEntity = mutableMapOf<UUID, Location>()

    private val entitiesByLocation = mutableMapOf<Location, MutableSet<UUID>>()
        .toSortedMap(
            compareByDescending(Location::y)
                .thenBy(Location::x)
        )

    private val tileByLocation = mutableMapOf<Location, Tile>()
        .toSortedMap(
            compareByDescending(Location::y)
                .thenBy(Location::x)
        )

    fun setEntityLocation(id: UUID, location: Location) {
        val oldLocation = locationByEntity[id]
        if (oldLocation != null && oldLocation != location) {
            entitiesByLocation[oldLocation]?.remove(id)
            log.trace("Moving entity $id from $oldLocation to $location")
        }

        entitiesByLocation
            .getOrPut(location) { mutableSetOf() }
            .add(id)

        locationByEntity[id] = location
    }

    // TODO: remove entity

    fun setTile(location: Location, tile: Tile) {
        tileByLocation[location] = tile
    }

    // TODO: remove tile

    fun forEachTile(action: (Location, Tile) -> Unit) {
        for ((location, tile) in tileByLocation) {
            action.invoke(location, tile)
        }
    }

    fun forEachEntity(action: (Location, Set<UUID>) -> Unit) {
        for ((location, entities) in entitiesByLocation) {
            action.invoke(location, entities)
        }
    }
}
