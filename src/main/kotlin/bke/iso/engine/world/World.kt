package bke.iso.engine.world

import bke.iso.engine.core.Events
import bke.iso.engine.core.EngineModule
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Entities

class World(events: Events) : EngineModule() {

    override val moduleName = "world"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    val entities = Entities(events)
    val buildings = Buildings()

    private val deletedEntities = mutableSetOf<Entity>()

    override fun update(deltaTime: Float) {
        for (actor in deletedEntities) {
            entities.delete(actor)
            buildings.remove(actor)
        }
        deletedEntities.clear()
    }

    fun delete(entity: Entity) {
        deletedEntities.add(entity)
    }

    fun clear() {
        entities.clear()
        buildings.clear()
    }
}
