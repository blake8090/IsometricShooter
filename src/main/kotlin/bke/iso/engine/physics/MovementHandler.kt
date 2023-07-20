package bke.iso.engine.physics

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventHandler
import bke.iso.engine.log
import bke.iso.engine.physics.collision.CollisionServiceV2
import bke.iso.engine.physics.collision.EntityBoxCollision
import com.badlogic.gdx.math.Vector3

class MovementHandler(private val collisionService: CollisionServiceV2) : EventHandler<MoveEvent> {
    override val type = MoveEvent::class

    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val delta = Vector3(event.dx, event.dy, 0f)
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
        if (collisions.isEmpty()) {
            entity.x += delta.x
            entity.y += delta.y
            entity.z += delta.z
            return
        }

        val collision = collisions.firstOrNull()
        if (collision == null) {
            entity.x += delta.x
            entity.y += delta.y
            entity.z += delta.z
            return
        }

        val collisionTime = collision.collisionTime
        val hitNormal = collision.hitNormal

        entity.x += delta.x * collisionTime
        entity.y += delta.y * collisionTime

        // perform slide response
        // TODO: fix bug with sliding into corners
        val remainingTime = 1f - collisionTime
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
