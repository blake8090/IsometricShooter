package bke.iso.engine.world.entity

import bke.iso.engine.math.Location
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedSet

class Grid {

    val actors = OrderedSet<Entity>()

    private val actorsByLocation = OrderedMap<Location, ObjectSet<Entity>>()
    private val locationsByEntity = ObjectMap<Entity, ObjectSet<Location>>()

    init {
        // improves performance when removing objects
        actors.orderedItems().ordered = false
    }

    operator fun get(location: Location): ObjectSet<Entity> =
        actorsByLocation[location] ?: ObjectSet()

    fun update(entity: Entity) {
        if (!actors.contains(entity)) {
            actors.add(entity)
        }

        removeLocations(entity)

        for (location in entity.getLocations()) {
            // TODO: add some verification here, like that there can't be more than one tile entity in a location
            getOrPutLocations(entity).add(location)
            getOrPutActors(location).add(entity)
        }
    }

    private fun getOrPutActors(location: Location): ObjectSet<Entity> {
        if (!actorsByLocation.containsKey(location)) {
            actorsByLocation.put(location, ObjectSet())
        }
        return actorsByLocation[location]
    }

    private fun getOrPutLocations(entity: Entity): ObjectSet<Location> {
        if (!locationsByEntity.containsKey(entity)) {
            locationsByEntity.put(entity, ObjectSet())
        }
        return locationsByEntity[entity]
    }

    fun delete(entity: Entity) {
        removeLocations(entity)
        actors.remove(entity)
    }

    private fun removeLocations(entity: Entity) {
        val locations = locationsByEntity.remove(entity) ?: return
        for (location in locations) {
            actorsByLocation[location]?.remove(entity)
        }
    }

    fun clear() {
        actors.clear()
        actorsByLocation.clear()
        locationsByEntity.clear()
    }
}
