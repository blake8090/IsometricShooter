package bke.iso.engine.physics

import bke.iso.engine.event.EventHandler
import bke.iso.engine.physics.collision.CollisionServiceV2
import bke.iso.engine.physics.collision.PredictedCollisions
import com.badlogic.gdx.math.Vector3

class MovementHandler(private val collisionService: CollisionServiceV2) : EventHandler<MoveEvent> {
    override val type = MoveEvent::class

    override fun handle(event: MoveEvent) {
        val entity = event.entity

        val delta = calculateDelta(event)
        val predictedCollisions = collisionService.predictEntityCollisions(entity, delta.x, delta.y, delta.z)
        if (predictedCollisions != null) {
            handleCollisions(delta, predictedCollisions)
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

    private fun handleCollisions(delta: Vector3, predictedCollisions: PredictedCollisions) {
        val collision = predictedCollisions.collisions
            .filter { it.data.solid }
            .sortedBy { it.collisionTime }
            .firstOrNull()

        if (collision == null || collision.collisionTime == 1f) {
            return
        }

        delta.scl(collision.collisionTime)
    }
}
