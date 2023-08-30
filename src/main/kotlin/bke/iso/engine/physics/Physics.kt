package bke.iso.engine.physics

import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.physics.collision.Collision
import bke.iso.engine.physics.collision.CollisionSide
import bke.iso.engine.physics.collision.PredictedCollision
import bke.iso.engine.physics.collision.getCollisionBox
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.abs

const val DEFAULT_GRAVITY: Float = -9.8f

class Physics(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        game.world.actorsWith { actor, body: PhysicsBody ->
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
        val collision = game.collisions.predictCollisions(actor, delta)
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

    private fun collidedWithGround(actor: Actor): Boolean {
        val groundCollision = game.collisions
            .getCollisions(actor)
            .sortedBy(Collision::distance)
            .filter { collision -> collision.side == CollisionSide.TOP }
            .firstOrNull { collision ->
                val type = getPhysicsMode(collision.obj)
                type == PhysicsMode.SOLID || type == PhysicsMode.KINEMATIC
            }
        return groundCollision != null
    }

    private fun getPhysicsMode(obj: GameObject) = (obj as? Actor)
        ?.get<PhysicsBody>()
        ?.mode
        ?: PhysicsMode.SOLID

//    private fun update(actor: Actor, velocity: Velocity, deltaTime: Float) {
//        ifNotNull(actor.get<Acceleration>()) { acceleration ->
//            velocity.x += acceleration.x * deltaTime
//            velocity.y += acceleration.y * deltaTime
//            velocity.z += acceleration.z * deltaTime
//        }
//
//        ifNotNull(actor.get<Gravity>()) { gravity ->
//            velocity.z += gravity.acceleration * deltaTime
//            velocity.z = max(velocity.z, gravity.terminalVelocity)
//        }
//
//        val delta = Vector3(
//            velocity.x * deltaTime,
//            velocity.y * deltaTime,
//            velocity.z * deltaTime
//        )
//        move(actor, delta)
//        handleGroundCollision(actor, velocity)
//
//        // TODO: implement after refactor
////        val upwardsCollision = game.collisions
////            .getCollisions(actor)
////            .filter { !it.solid && it.side == CollisionSide.BOTTOM && !(it.obj is Actor && it.obj.has<Shadow>()) }
////            .minByOrNull { it.distance }
////        if (upwardsCollision != null) {
////            //log.trace { "pushing up" }
////            val obj = upwardsCollision.obj
////            if (obj is Actor && velocity.z > 0f) {
////                val v = obj.get<Velocity>()!!
////                v.z = velocity.z
////                val data = actor.getCollisionData()!!
////                //move(obj, Vector3(0f, 0f, velocity.z * deltaTime))
////                obj.moveTo(obj.x, obj.y, data.box.max.z + (velocity.z * deltaTime))
////            }
////        }
//    }
//
//    private fun move(actor: Actor, delta: Vector3) {
//        if (delta.isZero) {
//            return
//        }
//
//        val collisions = game.collisions.predictCollisions(actor, delta)
//            .filter(PredictedCollision::solid)
//            .sortedWith(compareBy(PredictedCollision::collisionTime, PredictedCollision::distance))
//        val collision = collisions.firstOrNull()
//        if (collision == null) {
//            actor.move(delta)
//            return
//        }
//
//        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
//        actor.move(collisionDelta)
//
//        // sometimes an actor may clip into another game object like a wall or a ground tile.
//        // in case of an overlap, the actor's position should be reset to the outer edge of the object's collision box.
//        val box = checkNotNull(actor.getCollisionData()?.box) {
//            "Expected CollisionData for $actor"
//        }
//        if (box.getOverlapArea(collision.box) != 0f) {
//            log.trace { "Resolving overlap between $actor and ${collision.obj} on side: ${collision.side}" }
//            resolveOverlap(actor, box, collision)
//        }
//        killVelocity(actor, collision.hitNormal)
//        slide(actor, delta, collision.hitNormal)
//    }
//
//    private fun killVelocity(actor: Actor, hitNormal: Vector3) {
//        val velocity = requireNotNull(actor.get<Velocity>()) {
//            "Expected Velocity component for $actor"
//        }
//        velocity.x -= velocity.x * abs(hitNormal.x)
//        velocity.y -= velocity.y * abs(hitNormal.y)
//        velocity.z -= velocity.z * abs(hitNormal.z)
//    }
//
//    private fun slide(actor: Actor, delta: Vector3, hitNormal: Vector3) {
//        // first, eliminate motion towards solid object by projecting the motion on to the collision normal
//        val eliminatedMotion = Vector3(hitNormal).scl(delta.dot(hitNormal))
//        // then, subtract the eliminated motion from the original motion, thus producing a slide effect
//        val newDelta = Vector3(delta).sub(eliminatedMotion)
//        move(actor, newDelta)
//    }
//
//    private fun resolveOverlap(actor: Actor, box: Box, collision: PredictedCollision) {
//        var x = actor.x
//        var y = actor.y
//        var z = actor.z
//        when (collision.side) {
//            CollisionSide.LEFT -> {
//                x = collision.box.min.x - (box.size.x / 2f)
//            }
//
//            CollisionSide.RIGHT -> {
//                x = collision.box.max.x + (box.size.x / 2f)
//            }
//
//            CollisionSide.FRONT -> {
//                y = collision.box.min.y - (box.size.y / 2f)
//            }
//
//            CollisionSide.BACK -> {
//                y = collision.box.max.y + (box.size.y / 2f)
//            }
//
//            CollisionSide.TOP -> {
//                // an actor's origin is the bottom of the collision box, not the center
//                z = collision.box.max.z
//            }
//
//            CollisionSide.BOTTOM -> {
//                z = collision.box.min.z - (box.size.z / 2f)
//            }
//
//            CollisionSide.CORNER -> {
//                log.warn { "Could not resolve corner collision" }
//            }
//        }
//        actor.moveTo(x, y, z)
//    }
//
//    private fun handleGroundCollision(actor: Actor, velocity: Velocity) {
//        val collision = game.collisions
//            .getCollisions(actor)
//            .filter { collision -> collision.solid && collision.side == CollisionSide.TOP }
//            .minByOrNull(Collision::distance)
//            ?: return
//
//        val obj = collision.obj as? Actor ?: return
//        ifNotNull(obj.get<Velocity>()) { objVelocity ->
//            velocity.x += objVelocity.x
//            velocity.y += objVelocity.y
//            velocity.z += objVelocity.z
//        }
//    }
}
