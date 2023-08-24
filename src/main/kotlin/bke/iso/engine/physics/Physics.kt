package bke.iso.engine.physics

import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.Box2
import bke.iso.engine.world.Actor
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.max

const val GRAVITY_ACCELERATION = -9.8f
const val TERMINAL_VELOCITY = -10f

class Physics(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        game.world.actorsWith<Velocity> { actor, velocity ->
            update(actor, velocity, deltaTime)
        }
    }

    private fun update(actor: Actor, velocity: Velocity, deltaTime: Float) {
        val acceleration = actor.get<Acceleration>()
        if (acceleration != null) {
            velocity.x += acceleration.x * deltaTime
            velocity.y += acceleration.y * deltaTime
            velocity.z += acceleration.z * deltaTime
        }

        applyGravity(actor, velocity, deltaTime)

        val delta = Vector3(
            velocity.x * deltaTime,
            velocity.y * deltaTime,
            velocity.z * deltaTime
        )
        if (!delta.isZero) {
            move(actor, delta)
        }
    }

    private fun applyGravity(actor: Actor, velocity: Velocity, deltaTime: Float) {
        val gravity = actor.get<Gravity>() ?: return

        val c = actor.get<FrameCollisions>()
            ?.collisions
            ?.filter { collision -> collision.solid && collision.side == CollisionSide.TOP }
            ?.sortedBy(Collision::distance)
            ?.firstOrNull()
        if (c == null) {
            velocity.z += gravity.acceleration * deltaTime
            velocity.z = max(velocity.z, gravity.terminalVelocity)
        } else {
            velocity.z = 0f
        }
    }

    private fun move(actor: Actor, delta: Vector3) {
        if (delta.isZero) {
            return
        }

        // TODO: clean this up
        val collisions = game.collisions.predictCollisions(actor, delta)
            .sortedWith(
                compareBy(PredictedCollision::collisionTime)
                    .thenBy(PredictedCollision::distance)
            )
        val collision = collisions.firstOrNull()

        if (collision == null) {
            actor.move(delta)
            return
        }

        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
        // TODO: fix bug where vertical movement when moving horizontally clips through tiles
        //  (might need to make tile's collision box taller?)
        actor.move(collisionDelta)

        val box = actor.getCollisionData()!!.box
        if (box.getOverlapArea(collision.box) != 0f) {
            log.debug { "Resolving overlap between $actor and ${collision.obj} on side: ${collision.side}" }
            resolveOverlap(actor, box, collision)
        }

        killVelocity(actor, collision.side)
        slide(actor, delta, collision.hitNormal)
    }

    private fun resolveOverlap(actor: Actor, box: Box2, collision: PredictedCollision) {
        var x = actor.x
        var y = actor.y
        var z = actor.z
        when (collision.side) {
            CollisionSide.LEFT -> {
                x = collision.box.min.x - (box.size.x / 2f)
            }

            CollisionSide.RIGHT -> {
                x = collision.box.max.x + (box.size.x / 2f)
            }

            CollisionSide.FRONT -> {
                y = collision.box.min.y - (box.size.y / 2f)
            }

            CollisionSide.BACK -> {
                y = collision.box.max.y + (box.size.y / 2f)
            }

            CollisionSide.TOP -> {
                z = collision.box.max.z + (box.size.z / 2f)
            }

            CollisionSide.BOTTOM -> {
                z = collision.box.min.z - (box.size.z / 2f)
            }

            CollisionSide.CORNER -> {
                log.warn { "Could not resolve corner collision" }
            }
        }
        actor.moveTo(x, y, z)
    }

    private fun killVelocity(actor: Actor, side: CollisionSide) {
        val velocity = actor.get<Velocity>()!!
        when (side) {
            CollisionSide.RIGHT, CollisionSide.LEFT -> {
                velocity.x = 0f
            }

            CollisionSide.BACK, CollisionSide.FRONT -> {
                velocity.y = 0f
            }

            CollisionSide.BOTTOM, CollisionSide.TOP -> {
                velocity.z = 0f
            }

            CollisionSide.CORNER -> {
                velocity.x = 0f
                velocity.y = 0f
                velocity.z = 0f
            }
        }
    }

    private fun slide(actor: Actor, delta: Vector3, hitNormal: Vector3) {
        // first, eliminate motion towards solid object by projecting the motion on to the collision normal
        val eliminatedMotion = Vector3(hitNormal).scl(delta.dot(hitNormal))
        // then, subtract the eliminated motion from the original motion, thus producing a slide effect
        val newDelta = Vector3(delta).sub(eliminatedMotion)
        move(actor, newDelta)
    }
}
