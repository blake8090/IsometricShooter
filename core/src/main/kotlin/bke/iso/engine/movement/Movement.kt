package bke.iso.engine.movement

import bke.iso.engine.core.EngineModule
import bke.iso.engine.physics.GroundContact
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3
import kotlin.math.abs
import kotlin.math.sign

class Movement(private val world: World) : EngineModule() {

    override val moduleName: String = "movement"
    override val updateWhileLoading: Boolean = false
    override val profilingEnabled: Boolean = true

    override fun update(deltaTime: Float) {
        world.entities.each<MovementIntent> { entity, intent ->
            val direction = Vector3(intent.direction)
            move(entity, direction, deltaTime)
            entity.remove<MovementIntent>()
        }

        world.entities.each<JumpIntent> { entity, _ ->
            jump(entity)
            entity.remove<JumpIntent>()
        }
    }

    private fun move(entity: Entity, direction: Vector3, deltaTime: Float) {
        val properties = entity.get<MovementProperties>() ?: return
        val body = entity.get<PhysicsBody>() ?: return
        if (body.mode == PhysicsMode.SOLID) {
            return
        }

        when (properties.mode) {
            MovementMode.INSTANT -> updateInstant(body, properties, direction)
            MovementMode.ACCELERATED -> updateAccelerated(body, properties, direction, deltaTime)
        }
    }

    private fun updateInstant(
        body: PhysicsBody,
        properties: MovementProperties,
        direction: Vector3
    ) {
        val planarDirection = getPlanarDirection(direction)
        val speed = properties.maxSpeed * properties.speedMultiplier
        body.velocity.x = planarDirection.x * speed
        body.velocity.y = planarDirection.y * speed

        if (direction.z != 0f) {
            body.velocity.z = sign(direction.z) * speed
        }
    }

    private fun updateAccelerated(
        body: PhysicsBody,
        properties: MovementProperties,
        direction: Vector3,
        deltaTime: Float
    ) {
        val planarDirection = getPlanarDirection(direction)
        val speedChange = if (planarDirection.isZero) {
            properties.deceleration * deltaTime
        } else {
            properties.acceleration * deltaTime
        }

        val speed = properties.maxSpeed * properties.speedMultiplier
        body.velocity.x = approach(body.velocity.x, planarDirection.x * speed, speedChange)
        body.velocity.y = approach(body.velocity.y, planarDirection.y * speed, speedChange)

        if (direction.z != 0f) {
            body.velocity.z = approach(
                current = body.velocity.z,
                target = sign(direction.z) * speed,
                amount = properties.acceleration * deltaTime
            )
        }
    }

    private fun getPlanarDirection(direction: Vector3): Vector3 {
        val planarDirection = Vector3(direction.x, direction.y, 0f)
        if (!planarDirection.isZero) {
            planarDirection.nor()
        }
        return planarDirection
    }

    private fun approach(current: Float, target: Float, amount: Float): Float {
        if (amount <= 0f) {
            return current
        }

        val delta = target - current
        return if (abs(delta) <= amount) {
            target
        } else {
            current + sign(delta) * amount
        }
    }

    private fun jump(entity: Entity) {
        val jump = entity.get<JumpProperties>() ?: return
        val body = entity.get<PhysicsBody>() ?: return

        entity.get<GroundContact>() ?: return
        body.applyVelocityChange(Vector3(0f, 0f, jump.speed))
        entity.remove<GroundContact>()
    }
}
