package bke.iso.engine.world

import bke.iso.engine.core.Events
import bke.iso.engine.core.EngineModule
import bke.iso.engine.world.entity.Actor
import bke.iso.engine.world.entity.Actors

class World(events: Events) : EngineModule() {

    override val moduleName = "world"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    val actors = Actors(events)
    val buildings = Buildings()

    private val deletedActors = mutableSetOf<Actor>()

    override fun update(deltaTime: Float) {
        for (actor in deletedActors) {
            actors.delete(actor)
            buildings.remove(actor)
        }
        deletedActors.clear()
    }

    fun delete(actor: Actor) {
        deletedActors.add(actor)
    }

    fun clear() {
        actors.clear()
        buildings.clear()
    }
}
