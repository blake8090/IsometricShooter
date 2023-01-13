package bke.iso.engine.physics

import bke.iso.service.Transient
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.system.System

@Transient
class PhysicsSystem(
    private val entityService: EntityService,
    private val eventService: EventService
) : System {
    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Velocity::class) { entity, velocity ->
            if (velocity.x != 0f || velocity.y != 0f) {
                eventService.fire(MoveEvent(entity, velocity.x, velocity.y, velocity.speed, deltaTime))
            }
        }
    }
}
