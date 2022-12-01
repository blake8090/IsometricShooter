package bke.iso.engine.physics

import bke.iso.engine.Controller
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService

class PhysicsController(
    private val entityService: EntityService,
    private val eventService: EventService
) : Controller {
    override fun update(deltaTime: Float) {
        for (entity in entityService.getAll()) {
            if (entity.vx != 0f || entity.vy != 0f) {
                eventService.fire(MoveEvent(entity, entity.vx, entity.vy))
            }
        }
    }
}
