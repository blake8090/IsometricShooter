package bke.iso.engine.physics

import bke.iso.engine.Engine
import bke.iso.engine.event.EventHandler

class MovementHandler(
    private val engine: Engine,
    private val physicsService: PhysicsService
) : EventHandler<MoveEvent> {
    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val dx = event.dx * engine.deltaTime
        val dy = event.dy * engine.deltaTime
        physicsService.move(entity, dx, dy)
    }
}
