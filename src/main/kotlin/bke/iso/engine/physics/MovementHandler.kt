package bke.iso.engine.physics

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventHandler
import bke.iso.engine.log
import bke.iso.engine.physics.collision.BoxCollisionSide
import bke.iso.engine.physics.collision.CollisionServiceV2
import bke.iso.engine.physics.collision.EntityBoxCollision
import com.badlogic.gdx.math.Vector3

class MovementHandler(private val collisionService: CollisionServiceV2) : EventHandler<MoveEvent> {
    override val type = MoveEvent::class

    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val delta = Vector3(event.dx, event.dy, event.dz)
            .nor()
            .scl(event.speed * event.deltaTime)

        resolveCollisions(entity, delta)
    }

    private fun getPredictedCollisions(entity: Entity, delta: Vector3): Set<EntityBoxCollision> {
        val predictedCollisions = collisionService.predictEntityCollisions(entity, delta.x, delta.y, delta.z)
            ?: return emptySet()

        return predictedCollisions.collisions
            .filter { it.data.solid }
            .sortedWith(
                compareBy(EntityBoxCollision::distance)
                    .thenBy(EntityBoxCollision::collisionTime)
            )
            .toSet()
    }

    private fun resolveCollisions(entity: Entity, delta: Vector3) {
        val collisions = getPredictedCollisions(entity, delta)

        val collision = collisions.firstOrNull()
        if (collision == null) {
            entity.move(delta)
            return
        }

        val collisionTime = collision.collisionTime
        val hitNormal = collision.hitNormal

        log.trace("delta: $delta")
        val collisionDelta = Vector3(delta)
        /*
        Ensure that collisions on the X and Y axis do not affect the Z axis, and vice-versa.
        This allows entities to move up and down while pressing on the sides of a solid object.

        Entities will also be able to move left, right, forward and backward while pressing
        on the top or bottom of a solid object.
         */
        when (collision.side) {
            BoxCollisionSide.TOP, BoxCollisionSide.BOTTOM -> {
                collisionDelta.z *= collisionTime
            }

            else -> {
                collisionDelta.x *= collisionTime
                collisionDelta.y *= collisionTime
            }
        }
        entity.move(collisionDelta)

        // TODO: fix bug with sliding into corners
        // perform a slide response only for collisions on the X and Y axes
        if (collision.side != BoxCollisionSide.TOP && collision.side != BoxCollisionSide.BOTTOM) {
            val remainingTime = 1f - collisionTime
            // TODO: add comments explaining this math
            val dotProd = ((delta.x * hitNormal.y) + (delta.y * hitNormal.x)) * remainingTime
            val newDelta = Vector3(
                dotProd * hitNormal.y,
                dotProd * hitNormal.x,
                0f
            )
            log.trace("new delta: $newDelta")
            resolveCollisions(entity, newDelta)
        }
    }
}
