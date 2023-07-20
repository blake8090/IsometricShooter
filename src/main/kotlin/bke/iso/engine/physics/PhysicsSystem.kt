package bke.iso.engine.physics

import bke.iso.engine.event.EventService
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService

class PhysicsSystem(
    private val worldService: WorldService,
    private val eventService: EventService
) : System {
    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Velocity::class) { entity, velocity ->
            // TODO: handle z-axis
            if (velocity.x != 0f || velocity.y != 0f) {
                eventService.fire(MoveEvent(entity, velocity.x, velocity.y, 0f, velocity.speed, deltaTime))
            }
        }
    }
}
