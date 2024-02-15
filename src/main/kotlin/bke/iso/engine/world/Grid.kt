package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedSet
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pool.Poolable

class Grid {

    private val objectMap = OrderedMap<Location, GridData>()
    private val objects = OrderedSet<GameObject>()
    private val locationsByActor = ObjectMap<Actor, ObjectSet<Location>>()

    private val pool = object : Pool<GridData>() {
        override fun newObject() = GridData()
    }

    init {
        // improves performance when removing objects
        objects.orderedItems().ordered = false
    }

    // TODO: property
    fun getObjects() = objects

    fun update(actor: Actor) {
        if (!objects.contains(actor)) {
            objects.add(actor)
        }

        removeLocations(actor)
        for (location in actor.getLocations()) {
            val data = getOrPutData(location)
            data.actors.add(actor)
            getOrPutLocations(actor).add(location)
        }
    }

    private fun getOrPutLocations(actor: Actor): ObjectSet<Location> {
        if (!locationsByActor.containsKey(actor)) {
            locationsByActor.put(actor, ObjectSet())
        }
        return locationsByActor.get(actor)
    }

    fun remove(actor: Actor) {
        removeLocations(actor)
        objects.remove(actor)
    }

    private fun removeLocations(actor: Actor) {
        val locations = locationsByActor[actor] ?: return
        for (location in locations) {
            val data = checkNotNull(objectMap.get(location)) {
                "Expected GridData for $location from $actor"
            }
            data.actors.remove(actor)

            if (data.tile == null && data.actors.isEmpty) {
                pool.free(data)
                objectMap.remove(location)
            }
        }
        locationsByActor.remove(actor)
    }

    fun objectsAt(location: Location): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        objectMap[location]?.let { data ->
            objects.addAll(data.actors)
            data.tile?.let(objects::add)
        }
        return objects
    }

    // TODO: unit test and investigate using this to optimize sorting objects for rendering
    fun objectsByLocation(): Map<Location, Set<GameObject>> {
        val map = mutableMapOf<Location, MutableSet<GameObject>>()
        for (entry in objectMap) {
            val set = map.getOrPut(entry.key) { mutableSetOf() }
            entry.value.tile?.let(set::add)
            set.addAll(entry.value.actors)
        }
        return map
    }

    fun setTile(tile: Tile) {
        val data = getOrPutData(tile.location)
        data.tile = tile
        objects.add(tile)
    }

    private fun getOrPutData(location: Location): GridData {
        if (!objectMap.containsKey(location)) {
            objectMap.put(location, pool.obtain())
        }
        return objectMap.get(location)
    }

    fun clear() {
        objectMap.clear()
        objects.clear()
        locationsByActor.clear()
    }
}

data class GridData(
    val actors: ObjectSet<Actor> = ObjectSet(),
    var tile: Tile? = null
) : Poolable {

    override fun reset() {
        actors.clear()
        tile = null
    }
}
