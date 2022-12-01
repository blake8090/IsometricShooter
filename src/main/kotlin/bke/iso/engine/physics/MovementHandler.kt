package bke.iso.engine.physics

import bke.iso.engine.Engine
import bke.iso.engine.event.EventHandler
import bke.iso.engine.log

class MovementHandler(
    private val engine: Engine,
    private val collisionService: CollisionService
) : EventHandler<MoveEvent> {
    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val dx = event.dx * engine.deltaTime
        val dy = event.dy * engine.deltaTime
        entity.x += dx
        entity.y += dy

        collisionService.checkCollisions(entity)?.let { result ->
            if (result.collisions.isNotEmpty() || result.solidCollisions.isNotEmpty()) {
                log.trace(
                    "Entity collided with ${result.collisions.size} entities"
                            + " and ${result.solidCollisions.size} solid entities"
                )
            }
        }
    }
}
