package bke.iso.engine.physics

import bke.iso.engine.Engine
import bke.iso.engine.event.EventHandler

class MovementHandler(private val engine: Engine) : EventHandler<MoveEvent> {
    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val dx = event.dx * engine.deltaTime
        val dy = event.dy * engine.deltaTime
        entity.x += dx
        entity.y += dy
    }
}
