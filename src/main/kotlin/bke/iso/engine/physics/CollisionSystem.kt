package bke.iso.engine.physics

import bke.iso.engine.event.EventService
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService

class CollisionSystem(
    private val worldService: WorldService,
    private val eventService: EventService
) : System {

    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(FrameCollisions::class) { entity, frameCollisions ->
            for (collision in frameCollisions.collisions) {
                eventService.fire(CollisionEvent(entity, collision))
            }
        }
    }

    override fun onFrameEnd() {
        worldService.entities.withComponent(FrameCollisions::class) { entity, _ ->
            entity.remove<FrameCollisions>()
        }
    }
}
