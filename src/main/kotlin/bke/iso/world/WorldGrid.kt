package bke.iso.world

import bke.iso.util.getLogger
import bke.iso.world.asset.TileTemplate
import com.badlogic.gdx.math.Vector3

data class Tile(
    var texture: String,
    var collidable: Boolean = false
) {
    constructor(template: TileTemplate) : this(template.texture, template.collidable)
}

data class LocationData(
    var tile: Tile? = null,
    var entities: MutableSet<Int> = mutableSetOf()
)

/**
 * Represents a location on the world grid.
 */
data class Location(val x: Int, val y: Int)

class WorldGrid {
    private val log = getLogger()
    private val grid = mutableMapOf<Location, LocationData>()
    private val locationByEntityId = mutableMapOf<Int, Location>()

    fun getTile(location: Location) =
        grid[location]?.tile

    fun setTile(tile: Tile, location: Location) {
        val data = grid.getOrPut(location) { LocationData() }
        data.tile = tile
    }

    fun getEntityLocation(id: Int) = locationByEntityId[id]

    fun updateEntityLocation(id: Int, pos: Vector3) {
        val newLocation = Location(pos.x.toInt(), pos.y.toInt())

        val oldLocation = locationByEntityId[id]
        if (oldLocation != null && oldLocation != newLocation) {
            grid[oldLocation]
                ?.entities
                ?.remove(id)
            locationByEntityId.remove(id)
            log.debug("moving entity $id from $oldLocation to $newLocation")
        }
        grid.getOrPut(newLocation) { LocationData() }
            .entities
            .add(id)
        locationByEntityId[id] = newLocation
    }

    fun getAll() =
        grid.map { entry -> Pair(entry.key, entry.value) }

    fun clear() {
        grid.clear()
        locationByEntityId.clear()
    }
}
