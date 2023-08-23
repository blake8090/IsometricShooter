package bke.iso.engine.physics

import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.world.Actor
import com.badlogic.gdx.math.Vector3

class Physics(override val game: Game) : Module() {

    override fun update(deltaTime: Float) {
        game.world.actorsWith<Velocity> { actor, velocity ->
            val delta = Vector3(
                velocity.x * deltaTime,
                velocity.y * deltaTime,
                velocity.z * deltaTime
            )
            move(actor, delta)
        }
    }

    private fun move(actor: Actor, delta: Vector3) {
        // TODO: clean this up
        val collision = game.collisions
            .predictCollisions(actor, delta)
            .filter { collision -> collision.data.solid }
            .sortedWith(
                compareBy(PredictedCollision::collisionTime)
                    .thenBy(PredictedCollision::distance)
            )
            .firstOrNull()

        if (collision == null) {
            actor.move(delta)
            return
        }

        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
        // TODO: fix bug where vertical movement when moving horizontally clips through tiles
        //  (might need to make tile's collision box taller?)
        actor.move(collisionDelta)

        // TODO: kill velocity along hit normal
        slide(actor, delta, collision.hitNormal)
    }

    private fun slide(actor: Actor, delta: Vector3, hitNormal: Vector3) {
        // first, eliminate motion towards solid object by projecting the motion on to the collision normal
        val eliminatedMotion = Vector3(hitNormal).scl(delta.dot(hitNormal))
        // then, subtract the eliminated motion from the original motion, thus producing a slide effect
        val newDelta = Vector3(delta).sub(eliminatedMotion)
        move(actor, newDelta)
    }
}
