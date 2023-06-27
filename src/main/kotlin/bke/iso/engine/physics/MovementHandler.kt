package bke.iso.engine.physics

import bke.iso.engine.log
import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventHandler
import bke.iso.engine.event.EventService
import bke.iso.engine.physics.collision.Bounds
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
        if (predictedCollisions != null) {
            for (collision in predictedCollisions.collisions) {
                resolveCollision(entity, predictedCollisions.data.bounds, collision, delta)
            }
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

    private fun resolveCollision(entity: Entity, bounds: Bounds, collision: EntityBoxCollision, delta: Vector3) {
        if (!collision.data.solid) {
            return
        }
        val box = collision.data.box
        when (collision.side) {
            BoxCollisionSide.FRONT -> {
                entity.y = box.getMin().y - (bounds.dimensions.y / 2f)
                delta.y = 0f
            }

            BoxCollisionSide.BACK -> {
                entity.y = box.getMax().y + (bounds.dimensions.y / 2f)
                delta.y = 0f
            }

            BoxCollisionSide.LEFT -> {
                entity.x = box.getMin().x - (bounds.dimensions.x / 2f)
                delta.x = 0f
                log.trace("left collision")
            }

            BoxCollisionSide.RIGHT -> {
                entity.x = box.getMax().x + (bounds.dimensions.x / 2f)
                delta.x = 0f
                log.trace("right collision")
            }

            else -> {
                log.trace("Corner collision...?")
            }
        }
    }
}
