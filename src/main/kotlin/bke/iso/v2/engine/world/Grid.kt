package bke.iso.v2.engine.world

import bke.iso.v2.engine.math.Location
import bke.iso.v2.engine.render.Sprite

class Grid {

    private val actorGrid = mutableMapOf<Location, MutableSet<Actor>>()
    private val locationByActor = mutableMapOf<Actor, Location>()

    private val tileGrid = mutableMapOf<Location, Tile>()

    fun setTile(location: Location, sprite: Sprite, solid: Boolean) {
        val tile = Tile(sprite, solid, location)
        tileGrid[location] = tile
        tile.location = location
    }

    fun add(actor: Actor) =
        put(actor, Location(actor.x, actor.y, actor.z))

    private fun put(actor: Actor, location: Location) {
        locationByActor[actor] = location
        actorGrid
            .getOrPut(location) { mutableSetOf() }
            .add(actor)
    }

    fun move(actor: Actor) {
        remove(actor)
        add(actor)
    }

    fun remove(actor: Actor) {
        val location = locationByActor
            .remove(actor)
            ?: return
        actorGrid[location]?.remove(actor)
    }

    fun getAll(): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        for ((_, tile) in tileGrid) {
            objects.add(tile)
        }
        actorGrid.values.flatten().forEach(objects::add)
        return objects
    }

    fun getAll(location: Location): Set<GameObject> {
        val objects = mutableSetOf<GameObject>()
        tileGrid[location]?.let(objects::add)
        actorGrid[location]?.let(objects::addAll)
        return objects
    }

    fun getAllActors(): Set<Actor> =
        locationByActor.keys.toSet()
}
