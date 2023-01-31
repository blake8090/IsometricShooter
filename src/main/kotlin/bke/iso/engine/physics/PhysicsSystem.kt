package bke.iso.engine.physics

import bke.iso.service.Transient
import bke.iso.engine.event.EventService
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService

@Transient
class PhysicsSystem(
    private val worldService: WorldService,
    private val eventService: EventService
) : System {
    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Velocity::class) { entity, velocity ->
            if (velocity.x != 0f || velocity.y != 0f) {
                eventService.fire(MoveEvent(entity, velocity.x, velocity.y, velocity.speed, deltaTime))
            }
        }
    }
}
