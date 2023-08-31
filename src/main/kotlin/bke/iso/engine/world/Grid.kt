package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

class Grid {

    private val actorGrid = mutableMapOf<Location, MutableSet<Actor>>()
    private val locationsByActor = mutableMapOf<Actor, MutableSet<Location>>()

    private val tileGrid = mutableMapOf<Location, Tile>()

    val actors: Set<Actor> = locationsByActor.keys

    fun setTile(location: Location, sprite: Sprite) {
        val tile = Tile(sprite, location)
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
        locationsByActor.remove(actor)
    }

    fun getAll(): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        objects.addAll(tileGrid.values)
        for (actorSet in actorGrid.values) {
            objects.addAll(actorSet)
        }
        return objects
    }

    fun getAll(location: Location): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        tileGrid[location]?.let(objects::add)
        actorGrid[location]?.let(objects::addAll)
        return objects
    }
}
