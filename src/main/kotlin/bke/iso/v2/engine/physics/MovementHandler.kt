package bke.iso.v2.engine.physics

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.event.EventHandler
import bke.iso.v2.engine.event.EventService
import com.badlogic.gdx.math.Vector2

@Transient
class MovementHandler(
    private val collisionService: CollisionService,
    private val eventService: EventService
) : EventHandler<MoveEvent> {
    override val type = MoveEvent::class

    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val delta = calculateDelta(event)

        val result = collisionService.predictEntityCollisions(entity, delta.x, delta.y)
        if (result != null) {
            val bounds = result.bounds
            for (boxCollision in result.collisions) {
                resolveCollision(entity, bounds, boxCollision, delta)
                eventService.fire(CollisionEvent(entity, boxCollision.data))
            }
        }

        entity.x += delta.x
        entity.y += delta.y
    }

    private fun calculateDelta(event: MoveEvent): Vector2 {
        val delta = Vector2(event.dx, event.dy).nor()
        return Vector2(
            delta.x * event.speed * event.deltaTime,
            delta.y * event.speed * event.deltaTime
        )
    }

    private fun resolveCollision(entity: Entity, bounds: Bounds, collision: BoxCollision, delta: Vector2) {
        if (!collision.data.solid) {
            return
        }
        val box = collision.data.box
        when (collision.side) {
            CollisionSide.TOP -> {
                entity.y = box.y - bounds.length - bounds.offsetY
                delta.y = 0f
            }

            CollisionSide.BOTTOM -> {
                entity.y = (box.y + box.height) - bounds.offsetY
                delta.y = 0f
            }

            CollisionSide.LEFT -> {
                entity.x = (box.x + box.width) - bounds.offsetX
                delta.x = 0f
            }

            CollisionSide.RIGHT -> {
                entity.x = box.x - bounds.length - bounds.offsetX
                delta.x = 0f
            }

            else -> {
                log.trace("Corner collision...?")
            }
        }
    }
}
