package bke.iso.engine.world

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Events
import bke.iso.engine.core.EngineModule
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Entities

class World(
    events: Events,
    collisionBoxes: CollisionBoxes
) : EngineModule() {

    override val moduleName = "world"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    val entities = Entities(events, collisionBoxes)
    val buildings = Buildings(collisionBoxes)

    private val deletedEntities = mutableSetOf<Entity>()

    override fun update(deltaTime: Float) {
        for (entity in deletedEntities) {
            entities.delete(entity)
            buildings.remove(entity)
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
