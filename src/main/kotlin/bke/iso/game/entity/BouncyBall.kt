package bke.iso.game.entity

import bke.iso.engine.entity.Component
import bke.iso.engine.entity.Entity
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService
import kotlin.math.max

data class BouncyBall(var velocityZ: Float = 0f) : Component()

class BouncyBallSystem(private val worldService: WorldService) : System {

    private val force = 6f
    private val gravity = 6f
    private val terminalVelocity = -6f

    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(BouncyBall::class) { entity, bouncyBall ->
            if (onGround(entity)) {
                bouncyBall.velocityZ = force
            }

            val vz = bouncyBall.velocityZ * deltaTime
            entity.z += vz
            if (entity.z <= 0f) {
                entity.z = 0f
                bouncyBall.velocityZ = 0f
            }

            if (!onGround(entity)) {
                val newVelocity = bouncyBall.velocityZ - (gravity * deltaTime)
                bouncyBall.velocityZ = max(terminalVelocity, newVelocity)
            }
        }
    }

    private fun onGround(entity: Entity) =
        // TODO: do we really need to check < 0?
        entity.z <= 0f
}
