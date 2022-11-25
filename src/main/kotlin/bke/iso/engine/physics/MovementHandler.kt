package bke.iso.engine.physics

import bke.iso.engine.event.EventHandler

class MovementHandler : EventHandler<MoveEvent> {
    override fun handle(deltaTime: Float, event: MoveEvent) {
        val entity = event.entity
        val dx = event.dx * deltaTime
        val dy = event.dy * deltaTime
        entity.x += dx
        entity.y += dy
    }
}
