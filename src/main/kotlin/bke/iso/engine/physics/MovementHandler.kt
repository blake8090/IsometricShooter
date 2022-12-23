package bke.iso.engine.physics

import bke.iso.engine.Engine
import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventHandler
import bke.iso.engine.log
import com.badlogic.gdx.math.Vector2

class MovementHandler(
    private val engine: Engine,
    private val collisionService: CollisionService,
) : EventHandler<MoveEvent> {
    override val type = MoveEvent::class

    override fun handle(event: MoveEvent) {
        val entity = event.entity
        val delta = Vector2(
            event.dx * engine.deltaTime,
            event.dy * engine.deltaTime
        )

        val result = collisionService.predictEntityCollisions(entity, delta.x, delta.y)
        if (result != null) {
            val bounds = result.bounds
            for (boxCollision in result.collisions) {
                resolveCollision(entity, bounds, boxCollision, delta)
                engine.fireEvent(CollisionEvent(entity, boxCollision.data))
            }
        }

        entity.x += delta.x
        entity.y += delta.y
    }

    private fun resolveCollision(entity: Entity, bounds: Bounds, collision: BoxCollision, delta: Vector2) {
        if (!collision.data.solid) {
            return
        }
        val solidArea = collision.data.area
        when (collision.side) {
            CollisionSide.TOP -> {
                entity.y = solidArea.y - bounds.length - bounds.offsetY
                delta.y = 0f
            }

            CollisionSide.BOTTOM -> {
                entity.y = (solidArea.y + solidArea.height) - bounds.offsetY
                delta.y = 0f
            }

            CollisionSide.LEFT -> {
                entity.x = (solidArea.x + solidArea.width) - bounds.offsetX
                delta.x = 0f
            }

            CollisionSide.RIGHT -> {
                entity.x = solidArea.x - bounds.length - bounds.offsetX
                delta.x = 0f
            }

            else -> {
                log.trace("Corner collision...?")
            }
        }
    }
}
