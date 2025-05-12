package bke.iso.engine.world.actor

import bke.iso.engine.math.Location
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedSet

class Grid {

    val actors = OrderedSet<Actor>()

    private val actorsByLocation = OrderedMap<Location, ObjectSet<Actor>>()
    private val locationsByActor = ObjectMap<Actor, ObjectSet<Location>>()

    init {
        // improves performance when removing objects
        actors.orderedItems().ordered = false
    }

    operator fun get(location: Location): ObjectSet<Actor> =
        actorsByLocation[location] ?: ObjectSet()

    fun update(actor: Actor) {
        if (!actors.contains(actor)) {
            actors.add(actor)
        }

        removeLocations(actor)

        for (location in actor.getLocations()) {
            // TODO: add some verification here, like that there can't be more than one tile entity in a location
            getOrPutLocations(actor).add(location)
            getOrPutActors(location).add(actor)
        }
    }

    private fun getOrPutActors(location: Location): ObjectSet<Actor> {
        if (!actorsByLocation.containsKey(location)) {
            actorsByLocation.put(location, ObjectSet())
        }
        return actorsByLocation[location]
    }

    private fun getOrPutLocations(actor: Actor): ObjectSet<Location> {
        if (!locationsByActor.containsKey(actor)) {
            locationsByActor.put(actor, ObjectSet())
        }
        return locationsByActor[actor]
    }

    fun delete(actor: Actor) {
        removeLocations(actor)
        actors.remove(actor)
    }

    private fun removeLocations(actor: Actor) {
        val locations = locationsByActor.remove(actor) ?: return
        for (location in locations) {
            actorsByLocation[location]?.remove(actor)
        }
    }

    fun clear() {
        actors.clear()
        actorsByLocation.clear()
        locationsByActor.clear()
    }
}
