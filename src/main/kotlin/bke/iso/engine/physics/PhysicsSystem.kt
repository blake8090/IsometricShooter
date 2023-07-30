package bke.iso.engine.physics

import bke.iso.engine.entity.Component
import bke.iso.engine.entity.Entity
import bke.iso.engine.physics.collision.CollisionService
import bke.iso.engine.physics.collision.PredictedObjectCollision
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService
import com.badlogic.gdx.math.Vector3

data class Velocity(
    val delta: Vector3 = Vector3(),
    val speed: Vector3 = Vector3()
) : Component()

class PhysicsSystem(
    private val worldService: WorldService,
    private val collisionService: CollisionService
) : System {
    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Velocity::class) { entity, velocity ->
            val delta = Vector3(velocity.delta)
                .nor()
                .scl(velocity.speed)
                .scl(deltaTime)
            move(entity, delta)
        }
    }

    private fun move(entity: Entity, delta: Vector3) {
        val collision = collisionService
            .predictEntityCollisions(entity, delta.x, delta.y, delta.z)
            .filter { collision -> collision.data.solid }
            .sortedWith(
                compareBy(PredictedObjectCollision::collisionTime)
                    .thenBy(PredictedObjectCollision::distance)
            )
            .firstOrNull()

        if (collision == null) {
            entity.move(delta)
            return
        }

        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
        // TODO: fix bug where vertical movement when moving horizontally clips through tiles
        //  (might need to make tile's collision box taller?)
        entity.move(collisionDelta)

        // TODO: kill velocity along hit normal
        slide(entity, delta, collision.hitNormal)
    }

    private fun slide(entity: Entity, delta: Vector3, hitNormal: Vector3) {
        // first, eliminate motion towards solid object by projecting the motion on to the collision normal
        val eliminatedMotion = Vector3(hitNormal).scl(delta.dot(hitNormal))
        // then, subtract the eliminated motion from the original motion, thus producing a slide effect
        val newDelta = Vector3(delta).sub(eliminatedMotion)
        move(entity, newDelta)
    }
}
