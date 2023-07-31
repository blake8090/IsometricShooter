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
        get() {
            val gameObjects = mutableSetOf<GameObject>()
            grid.values.forEach { (tile, actors) ->
                tile?.let(gameObjects::add)
                gameObjects.addAll(actors)
            }
            return gameObjects.toSet()
        }

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
        val entry = grid.getOrPut(location) { Entry() }
        when (gameObject) {
            is Tile -> entry.tile = gameObject
            is Actor -> entry.actors.add(gameObject)
        }
    }

    fun remove(gameObject: GameObject) {
        val location = locationByObject.remove(gameObject)
        checkNotNull(location) {
            "GameObject $gameObject does not have a location"
        }

        val entry = grid[location]
        checkNotNull(entry) {
            "GameObject $gameObject not found in grid location $location"
        }
        entry.actors.remove(gameObject)
    }
}
