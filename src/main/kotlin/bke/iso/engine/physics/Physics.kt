package bke.iso.engine.physics

import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.PredictedCollision
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.EngineModule
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.abs

const val DEFAULT_GRAVITY: Float = -12f

class Physics(
    private val world: World,
    private val collisions: Collisions
) : EngineModule() {

    private val log = KotlinLogging.logger {}

    override val moduleName = "physics"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    override fun update(deltaTime: Float) {
        world.entities.each { actor, body: PhysicsBody ->
            update(actor, body, deltaTime)
        }
    }

    private fun update(entity: Entity, body: PhysicsBody, deltaTime: Float) {
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
        move(entity, body, delta)
    }

    private fun move(entity: Entity, body: PhysicsBody, delta: Vector3) {
        val predictCollisions = collisions.predictCollisions(entity, delta)
        // TODO: how to resolve multiple collisions to avoid objects falling out of the world?
        val collision = predictCollisions
            .sortedWith(compareBy(PredictedCollision::collisionTime, PredictedCollision::distance))
            .firstOrNull { collision -> getPhysicsMode(collision.entity) != PhysicsMode.GHOST }

        if (collision == null) {
            entity.move(delta)
            return
        }

        when (body.mode) {
            PhysicsMode.DYNAMIC -> solveDynamicContact(entity, body, delta, collision)
            PhysicsMode.KINEMATIC -> solveKinematicContact(entity, body, delta, collision)
            else -> entity.move(delta)
        }
    }

    private fun solveDynamicContact(entity: Entity, body: PhysicsBody, delta: Vector3, collision: PredictedCollision) {
        // move to the position where the collision occurred
        val collisionDelta = Vector3(delta).scl(collision.collisionTime)
        entity.move(collisionDelta)

        // make sure actor doesn't overlap the collision side
        clampPosToCollisionSide(entity, collision)

        // erase velocity towards collision normal
        body.velocity.x -= (body.velocity.x * abs(collision.hitNormal.x))
        body.velocity.y -= (body.velocity.y * abs(collision.hitNormal.y))
        body.velocity.z -= (body.velocity.z * abs(collision.hitNormal.z))

        // set velocity when landing on top of a moving kinematic (i.e. a moving platform)
        if (collision.side == CollisionSide.TOP && getPhysicsMode(collision.entity) == PhysicsMode.KINEMATIC) {
            val other = collision.entity
            val otherBody = other.get<PhysicsBody>()!!
            body.velocity.z = otherBody.velocity.z
        }

        // erase delta towards collision normal to get remaining movement for this frame
        val newDelta = Vector3(
            delta.x - (delta.x * abs(collision.hitNormal.x)),
            delta.y - (delta.y * abs(collision.hitNormal.y)),
            delta.z - (delta.z * abs(collision.hitNormal.z))
        )
        move(entity, body, newDelta)
    }

    private fun solveKinematicContact(entity: Entity, body: PhysicsBody, delta: Vector3, collision: PredictedCollision) {
        entity.move(delta)

        // bit of a hack to make vertical moving platforms work.
        // the dynamic actor handles pushing down on the kinematic,
        // while the kinematic pushes the dynamic actor upwards.
        // TODO: find a way to make this logic more generalized
        val other = collision.entity
        val otherBody = other.get<PhysicsBody>() ?: return
        if (otherBody.mode != PhysicsMode.DYNAMIC) {
            return
        }

        otherBody.velocity.z = body.velocity.z
        otherBody.forces.add(Vector3(0f, 0f, body.velocity.z))

        val box = checkNotNull(entity.getCollisionBox()) {
            "Expected collision box for $entity"
        }
        // make sure object doesn't clip through the platform next frame
        other.moveTo(other.x, other.y, box.max.z)
    }

    private fun clampPosToCollisionSide(entity: Entity, collision: PredictedCollision) {
        val box = checkNotNull(entity.getCollisionBox()) {
            "Expected collision box for $entity"
        }
        val otherBox = checkNotNull(collision.entity.getCollisionBox()) {
            "Expected collision box for ${collision.entity}"
        }

        var x = entity.x
        var y = entity.y
        var z = entity.z
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
        entity.moveTo(x, y, z)
    }
}

fun getPhysicsMode(entity: Entity) =
    entity
        .get<PhysicsBody>()
        ?.mode
        ?: PhysicsMode.SOLID
