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

    /**
     * Updates the given [entity]'s position in the grid.
     * @return false if the [entity]'s locations did not change, and true otherwise
     */
    fun update(entity: Entity): Boolean {
        if (!entities.contains(entity)) {
            entities.add(entity)
        }

        val previousLocations = locationsByEntity[entity]
        val newLocations = entity.getLocations()

        if (!locationsChanged(previousLocations, newLocations)) {
            return false
        }

        removeLocations(entity)

        for (location in newLocations) {
            getOrPutLocations(entity).add(location)
            getOrPutEntities(location).add(entity)
        }

        return true
    }

    private fun locationsChanged(previous: ObjectSet<Location>?, new: Set<Location>): Boolean {
        if (previous == null) {
            return true
        }

        for (location in new) {
            if (!previous.contains(location)) {
                return true
            }
        }

        return false
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
