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

    private val onGround = mutableMapOf<Actor, Boolean>()

    override fun update(deltaTime: Float) {
        game.world.actorsWith { actor, physicsBody: PhysicsBody ->
            val motion = actor.get<Motion>() ?: return@actorsWith
            motion.velocity.x += motion.acceleration.x * deltaTime
            motion.velocity.y += motion.acceleration.y * deltaTime
            motion.velocity.z += motion.acceleration.z * deltaTime

            if (physicsBody.bodyType == BodyType.DYNAMIC) {
                motion.velocity.z += DEFAULT_GRAVITY * deltaTime
            }

            applyImpulse(actor, physicsBody.bodyType, motion, deltaTime)

            val delta = Vector3(motion.velocity).scl(deltaTime)
            move(actor, physicsBody, motion, delta)

            if (physicsBody.bodyType == BodyType.DYNAMIC) {
                val collided = collidedWithGround(actor)
                val wasOnGround = onGround[actor] ?: false
                if (collided && !wasOnGround) {
                    log.trace { "just landed on ground" }
                } else if (!collided && wasOnGround) {
                    log.trace { "just left ground" }
                }
                onGround[actor] = collided
            }
        }
    }

    private fun applyImpulse(actor: Actor, bodyType: BodyType, motion: Motion, deltaTime: Float) {
        if (bodyType == BodyType.GHOST || bodyType == BodyType.KINEMATIC || bodyType == BodyType.SOLID) {
            return
        }
        val impulse = actor.get<Impulse>() ?: return
        if (impulse.x != 0f) {
            motion.velocity.x = impulse.x * deltaTime
        }
        if (impulse.y != 0f) {
            motion.velocity.y = impulse.y * deltaTime
        }
        if (impulse.z != 0f) {
            motion.velocity.z = impulse.z
        }
        actor.remove<Impulse>()
        log.trace { "applied impulse $impulse $actor" }
    }

    private fun move(actor: Actor, physicsBody: PhysicsBody, motion: Motion, delta: Vector3) {
        if (delta.isZero) {
            return
        }

        val collision = game.collisions.predictCollisions(actor, delta)
            .sortedWith(compareBy(PredictedCollision::collisionTime, PredictedCollision::distance))
            .firstOrNull { collision -> getBodyType(collision.obj) != BodyType.GHOST }
        // TODO: handle Kinematic collisions
        if (collision == null || !shouldCollide(physicsBody.bodyType, collision)) {
            actor.move(delta)
            return
        }

        /*
        collision responses:
        dynamic -> dynamic: ???
        dynamic -> solid: kill velocity along collision normal and try moving again
        dynamic -> kinematic: kill velocity along collision normal and try moving again
        kinematic -> dynamic: apply impulse!
        kinematic -> solid: nothing
         */
        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
        actor.move(collisionDelta)

        // an overlap doesn't happen all the time, but it doesn't hurt to double-check each frame
        resolveOverlap(actor, collision)

        // cancel out velocity along collision direction
        motion.velocity.x -= (motion.velocity.x * abs(collision.hitNormal.x))
        motion.velocity.y -= (motion.velocity.y * abs(collision.hitNormal.y))
        motion.velocity.z -= (motion.velocity.z * abs(collision.hitNormal.z))

        // move actor with the remaining delta
        val newDelta = Vector3(
            delta.x - (delta.x * abs(collision.hitNormal.x)),
            delta.y - (delta.y * abs(collision.hitNormal.y)),
            delta.z - (delta.z * abs(collision.hitNormal.z))
        )
        move(actor, physicsBody, motion, newDelta)
    }

    private fun shouldCollide(bodyType: BodyType, collision: PredictedCollision): Boolean {
        val objType = getBodyType(collision.obj)
        return bodyType == BodyType.DYNAMIC && objType != BodyType.GHOST
    }

    private fun resolveOverlap(actor: Actor, collision: PredictedCollision) {
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
                z = otherBox.max.z // an actor's origin is the bottom of the collision box, not the center
            }

            CollisionSide.BOTTOM -> {
                z = otherBox.min.z - box.size.z // an actor's origin is the bottom of the collision box, not the center
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
                val type = getBodyType(collision.obj)
                type == BodyType.SOLID || type == BodyType.KINEMATIC
            }
        return groundCollision != null
    }

    private fun getBodyType(obj: GameObject) = (obj as? Actor)
        ?.get<PhysicsBody>()
        ?.bodyType
        ?: BodyType.SOLID

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
