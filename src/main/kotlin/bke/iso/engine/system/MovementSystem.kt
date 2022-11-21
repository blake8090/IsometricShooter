package bke.iso.engine.system

import bke.iso.engine.entity.Entities

class MovementSystem(private val entities: Entities) : System {
    override fun update(deltaTime: Float) {
        entities.withComponent(Velocity::class) { entity, velocity ->
            entity.addComponent(Velocity(
                velocity.dx * deltaTime,
                velocity.dy * deltaTime
            ))
        }
    }
}
