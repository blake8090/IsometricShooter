package bke.iso.engine.physics

import bke.iso.engine.Controller
import bke.iso.engine.Engine
import bke.iso.engine.entity.EntityService

class PhysicsController(
    private val entityService: EntityService,
    private val engine: Engine
) : Controller {
    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Velocity::class) { entity, velocity ->
            if (velocity.x != 0f || velocity.y != 0f) {
                engine.fireEvent(MoveEvent(entity, velocity.x, velocity.y, velocity.speed))
            }
        }
    }
}
