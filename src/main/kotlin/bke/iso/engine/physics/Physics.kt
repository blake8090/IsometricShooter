package bke.iso.engine.physics

import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.PredictedCollision
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.abs

const val DEFAULT_GRAVITY: Float = -12f

class Physics(
    private val world: World,
    private val collisions: Collisions
) {

    private val log = KotlinLogging.logger {}

    fun update(deltaTime: Float) {
        world.actors.each { actor, body: PhysicsBody ->
            update(actor, body, deltaTime)
        }
    }

    private fun update(actor: Actor, body: PhysicsBody, deltaTime: Float) {
        val delta = Vector3()

        if (body.mode == PhysicsMode.DYNAMIC) {
            body.velocity.z += DEFAULT_GRAVITY * deltaTime
            for (force in body.forces) {
                force.scl(deltaTime)
                delta.add(force)
            }
            body.forces.clear()
        }

        delta.add(
            body.velocity.x * deltaTime,
            body.velocity.y * deltaTime,
            body.velocity.z * deltaTime
        )
        move(actor, body, delta)
    }

    private fun move(actor: Actor, body: PhysicsBody, delta: Vector3) {
        val predictCollisions = collisions.predictCollisions(actor, delta)
        // TODO: how to resolve multiple collisions to avoid objects falling out of the world?
        val collision = predictCollisions
            .sortedWith(compareBy(PredictedCollision::collisionTime, PredictedCollision::distance))
            .firstOrNull { collision -> getPhysicsMode(collision.obj) != PhysicsMode.GHOST }

        if (collision == null) {
            actor.move(delta)
            return
        }

        when (body.mode) {
            PhysicsMode.DYNAMIC -> solveDynamicContact(actor, body, delta, collision)
            PhysicsMode.KINEMATIC -> solveKinematicContact(actor, body, delta, collision)
            else -> actor.move(delta)
        }
    }

    private fun solveDynamicContact(actor: Actor, body: PhysicsBody, delta: Vector3, collision: PredictedCollision) {
        // move to the position where the collision occurred
        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
        actor.move(collisionDelta)

        // make sure actor doesn't overlap the collision side
        clampPosToCollisionSide(actor, collision)

        // erase velocity towards collision normal
        body.velocity.x -= (body.velocity.x * abs(collision.hitNormal.x))
        body.velocity.y -= (body.velocity.y * abs(collision.hitNormal.y))
        body.velocity.z -= (body.velocity.z * abs(collision.hitNormal.z))

        // set velocity when landing on top of a moving kinematic (i.e. a moving platform)
        if (collision.side == CollisionSide.TOP && getPhysicsMode(collision.obj) == PhysicsMode.KINEMATIC) {
            val obj = collision.obj as Actor
            val objBody = obj.get<PhysicsBody>()!!
            body.velocity.z = objBody.velocity.z
        }

        // erase delta towards collision normal to get remaining movement for this frame
        val newDelta = Vector3(
            delta.x - (delta.x * abs(collision.hitNormal.x)),
            delta.y - (delta.y * abs(collision.hitNormal.y)),
            delta.z - (delta.z * abs(collision.hitNormal.z))
        )
        move(actor, body, newDelta)
    }

    private fun solveKinematicContact(actor: Actor, body: PhysicsBody, delta: Vector3, collision: PredictedCollision) {
        actor.move(delta)

        // bit of a hack to make vertical moving platforms work.
        // the dynamic actor handles pushing down on the kinematic,
        // while the kinematic pushes the dynamic actor upwards.
        // TODO: find a way to make this logic more generalized
        val obj = collision.obj as? Actor ?: return
        val objBody = obj.get<PhysicsBody>() ?: return
        if (objBody.mode != PhysicsMode.DYNAMIC) {
            return
        }

        objBody.velocity.z = body.velocity.z
        objBody.forces.add(Vector3(0f, 0f, body.velocity.z))

        val box = checkNotNull(actor.getCollisionBox()) {
            "Expected collision box for $actor"
        }
        // make sure object doesn't clip through the platform next frame
        obj.moveTo(obj.x, obj.y, box.max.z)
    }

    private fun clampPosToCollisionSide(actor: Actor, collision: PredictedCollision) {
        val box = checkNotNull(actor.getCollisionBox()) {
            "Expected collision box for $actor"
        }
        val otherBox = checkNotNull(collision.obj.getCollisionBox()) {
            "Expected collision box for $collision.obj"
        }

        var x = actor.x
        var y = actor.y
        var z = actor.z
        when (collision.side) {
            CollisionSide.LEFT -> {
                x = otherBox.min.x - (box.size.x / 2f)
            }

            CollisionSide.RIGHT -> {
                x = otherBox.max.x + (box.size.x / 2f)
            }

            CollisionSide.FRONT -> {
                y = otherBox.min.y - (box.size.y / 2f)
            }

            CollisionSide.BACK -> {
                y = otherBox.max.y + (box.size.y / 2f)
            }

            CollisionSide.TOP -> {
                z = otherBox.max.z // actor origins are at the bottom of the collision box, not the center
            }

            CollisionSide.BOTTOM -> {
                z = otherBox.min.z - box.size.z // actor origins are at the bottom of the collision box, not the center
            }

            CollisionSide.CORNER -> {
                log.warn { "Could not resolve corner collision" }
            }
        }
        actor.moveTo(x, y, z)
    }
}

fun getPhysicsMode(obj: GameObject) = (obj as? Actor)
    ?.get<PhysicsBody>()
    ?.mode
    ?: PhysicsMode.SOLID