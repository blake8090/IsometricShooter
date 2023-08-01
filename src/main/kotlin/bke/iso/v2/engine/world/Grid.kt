package bke.iso.v2.engine.world

import bke.iso.engine.math.Location

private data class Entry(
    var tile: Tile? = null,
    val actors: MutableSet<Actor> = mutableSetOf()
)

class Grid {

    private val locationByObject = mutableMapOf<GameObject, Location>()
    private val grid = mutableMapOf<Location, Entry>()

    val objects: Set<GameObject>
        get() = locationByObject.keys.toSet()

    fun add(gameObject: GameObject) =
        put(gameObject, Location(gameObject.x, gameObject.y, gameObject.z))

    fun move(gameObject: GameObject, newLocation: Location) {
        remove(gameObject)
        put(gameObject, newLocation)
    }

    private fun put(gameObject: GameObject, location: Location) {
        val existingLocation = locationByObject[gameObject]
        require(existingLocation == null) {
            "GameObject $gameObject is already in location $existingLocation"
        }

        locationByObject[gameObject] = location
        val entry = grid.getOrPut(location) { Entry() }
        when (gameObject) {
            is Tile -> entry.tile = gameObject
            is Actor -> entry.actors.add(gameObject)
        }
    }

    fun remove(gameObject: GameObject) {
        val location = locationByObject.remove(gameObject) ?: return
        val entry = grid[location] ?: return
        when (gameObject) {
            is Tile -> entry.tile = null
            is Actor -> entry.actors.remove(gameObject)
        }
    }

    fun getAll(location: Location): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        val (tile, actors) = grid[location] ?: return emptySet()
        tile?.let(objects::add)
        objects.addAll(actors)
        return objects
    }
}
