package bke.iso.engine.world.entity

import bke.iso.engine.math.Location
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedSet

class Grid {

    val entities = OrderedSet<Entity>()

    private val entitiesByLocation = OrderedMap<Location, ObjectSet<Entity>>()
    private val locationsByEntity = ObjectMap<Entity, ObjectSet<Location>>()

    init {
        // improves performance when removing objects
        entities.orderedItems().ordered = false
    }

    operator fun get(location: Location): ObjectSet<Entity> =
        entitiesByLocation[location] ?: ObjectSet()

    fun update(entity: Entity) {
        if (!entities.contains(entity)) {
            entities.add(entity)
        }

        removeLocations(entity)

        for (location in entity.getLocations()) {
            // TODO: add some verification here, like that there can't be more than one tile entity in a location
            getOrPutLocations(entity).add(location)
            getOrPutEntities(location).add(entity)
        }
    }

    private fun getOrPutEntities(location: Location): ObjectSet<Entity> {
        if (!entitiesByLocation.containsKey(location)) {
            entitiesByLocation.put(location, ObjectSet())
        }
        return entitiesByLocation[location]
    }

    private fun getOrPutLocations(entity: Entity): ObjectSet<Location> {
        if (!locationsByEntity.containsKey(entity)) {
            locationsByEntity.put(entity, ObjectSet())
        }
        return locationsByEntity[entity]
    }

    fun delete(entity: Entity) {
        removeLocations(entity)
        entities.remove(entity)
    }

    private fun removeLocations(entity: Entity) {
        val locations = locationsByEntity.remove(entity) ?: return
        for (location in locations) {
            entitiesByLocation[location]?.remove(entity)
        }
    }

    fun clear() {
        entities.clear()
        entitiesByLocation.clear()
        locationsByEntity.clear()
    }
}
