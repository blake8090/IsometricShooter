package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

// TODO: negative coordinates NOT ALLOWED!

class Grid {

    private val actorGrid = mutableMapOf<Location, MutableSet<Actor>>()
    private val locationsByActor = mutableMapOf<Actor, MutableSet<Location>>()

    private val tileGrid = mutableMapOf<Location, Tile>()

    fun setTile(location: Location, sprite: Sprite, solid: Boolean) {
        val tile = Tile(sprite, solid, location)
        tileGrid[location] = tile
        tile.location = location
    }

    fun update(actor: Actor) {
        remove(actor)
        for (location in actor.getLocations()) {
            actorGrid
                .getOrPut(location) { mutableSetOf() }
                .add(actor)
            locationsByActor
                .getOrPut(actor) { mutableSetOf() }
                .add(location)
        }
    }

    fun remove(actor: Actor) {
        val locations = locationsByActor[actor] ?: return
        for (location in locations) {
            actorGrid[location]?.remove(actor)
        }
    }

    fun getAll(): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        objects.addAll(tileGrid.values)
        actorGrid.values.forEach(objects::addAll)
        return objects
    }

    fun getAll(location: Location): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        tileGrid[location]?.let(objects::add)
        actorGrid[location]?.let(objects::addAll)
        return objects
    }

    fun getAllActors()  =
        locationsByActor.keys
}
