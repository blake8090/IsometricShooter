package bke.iso.engine.world

import bke.iso.engine.core.Events
import bke.iso.engine.core.EngineModule
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors

class World(events: Events) : EngineModule() {

    override val moduleName = "world"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    private val grid = Grid()

    val actors = Actors(grid, events)
    val buildings = Buildings()

    private val deletedActors = mutableSetOf<Actor>()

    // TODO: property?
    fun getObjects() = grid.actors

    override fun update(deltaTime: Float) {
        for (actor in deletedActors) {
            grid.delete(actor)
            buildings.remove(actor)
        }
        deletedActors.clear()
    }

    fun delete(actor: Actor) {
        deletedActors.add(actor)
    }

    fun clear() {
        grid.clear()
        buildings.clear()
    }
}
