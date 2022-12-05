package bke.iso.engine.physics

import bke.iso.engine.Controller
import bke.iso.engine.Engine
import bke.iso.engine.entity.EntityService

class PhysicsController(
    private val entityService: EntityService,
    private val engine: Engine
) : Controller {
    override fun update(deltaTime: Float) {
        for (entity in entityService.getAll()) {
            if (entity.vx != 0f || entity.vy != 0f) {
                engine.fireEvent(MoveEvent(entity, entity.vx, entity.vy))
            }
        }
    }
}
