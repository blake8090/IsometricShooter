package bke.iso.v2.engine.world

import java.util.*

/**
 * Represents a specific location on the world grid.
 */
data class Location(val x: Int, val y: Int)

class WorldGrid {
    /**
     * A 2D grid of tiles and entities.
     *
     * Each location, i.e. (3, 1), contains exactly one tile and multiple entities.
     *
     * Locations in the grid are first sorted by their y value, and then the x value.
     * This allows for properly rendering elements in an isometric projection.
     */
    private val dataByLocation =
        mutableMapOf<Location, GridData>()
            .toSortedMap(
                compareBy<Location> { it.y }.thenBy { it.x }
            )

    /**
     * Maps an entity ID to a location.
     * Used for keeping track of entities and their locations within the world grid.
     */
    private val locationByEntity = mutableMapOf<UUID, Location>()

    fun setEntityLocation(id: UUID, location: Location) {
        val oldLocation = locationByEntity[id]
        if (oldLocation != null && oldLocation != location) {
            dataByLocation[oldLocation]
                ?.entities
                ?.remove(id)
        }

        dataByLocation.getOrPut(location) { GridData() }
            .entities
            .add(id)

        locationByEntity[id] = location
    }

    // TODO: remove entity

    fun getTile(location: Location): Tile? =
        dataByLocation[location]
            ?.tile

    fun setTile(location: Location, tile: Tile) {
        dataByLocation
            .getOrPut(location) { GridData() }
            .tile = tile
    }

    // TODO: remove tile

    fun forEach(action: (Location, Tile?, Set<UUID>) -> Unit) {
        dataByLocation.forEach { (location, data) ->
            action.invoke(location, data.tile, data.entities.toSet())
        }
    }
}

private class GridData {
    var tile: Tile? = null
    val entities = mutableListOf<UUID>()
}
