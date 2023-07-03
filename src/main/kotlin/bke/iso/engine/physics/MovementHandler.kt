package bke.iso.engine.physics

import bke.iso.engine.log
import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventHandler
import bke.iso.engine.event.EventService
import bke.iso.engine.physics.collision.BoxCollisionSide
import bke.iso.engine.physics.collision.CollisionServiceV2
import bke.iso.engine.physics.collision.EntityBoxCollision
import com.badlogic.gdx.math.Vector3

class MovementHandler(
    private val collisionService: CollisionServiceV2,
    private val eventService: EventService
) : EventHandler<MoveEvent> {
    override val type = MoveEvent::class

    override fun handle(event: MoveEvent) {
        val entity = event.entity

        val delta = calculateDelta(event)
        val predictedCollisions = collisionService.predictEntityCollisions(entity, delta.x, delta.y, delta.z)
        if (predictedCollisions != null && predictedCollisions.collisions.isNotEmpty()) {
            log.trace("begin")
            val solidCollision = predictedCollisions.collisions
                .sortedBy { it.distance }
                .firstOrNull { it.data.solid }
            if (solidCollision != null) {
                entity.x = solidCollision.intersection.x
                entity.y = solidCollision.intersection.y
                entity.z = solidCollision.intersection.z
                when(solidCollision.side) {
                    BoxCollisionSide.TOP, BoxCollisionSide.BOTTOM -> {
                        delta.z = 0f
                    }

                    BoxCollisionSide.FRONT, BoxCollisionSide.BACK -> {
                        delta.y = 0f
                    }

                    BoxCollisionSide.LEFT, BoxCollisionSide.RIGHT -> {
                        delta.x = 0f
                    }

                    BoxCollisionSide.CORNER -> {}
                }
            }
            log.trace("end")
        }

        entity.x += delta.x
        entity.y += delta.y
    }

    private fun calculateDelta(event: MoveEvent): Vector3 {
        val delta = Vector3(event.dx, event.dy, 0f).nor()
        return Vector3(
            delta.x * event.speed * event.deltaTime,
            delta.y * event.speed * event.deltaTime,
            0f
        )
    }

    private fun resolveCollision(entity: Entity, collision: EntityBoxCollision, delta: Vector3) {
        val intersection = collision.intersection
        log.trace("collision: ${collision.side}, dist: ${collision.distance}")
        when(collision.side) {
            BoxCollisionSide.LEFT -> {
                entity.x = intersection.x - 0.0001f
                delta.x = 0f
            }
            BoxCollisionSide.RIGHT -> {
                entity.x = intersection.x + 0.0001f
                delta.x = 0f
            }

            BoxCollisionSide.FRONT -> {
                entity.y = intersection.y - 0.0001f
                delta.y = 0f
            }

            BoxCollisionSide.BACK -> {
                entity.y = intersection.y + 0.0001f
                delta.y = 0f
            }

            else -> {}
        }
    }
}
