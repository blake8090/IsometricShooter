package bke.iso.v2.engine.physics

import bke.iso.service.Transient
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.event.EventService
import bke.iso.v2.engine.system.System

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
