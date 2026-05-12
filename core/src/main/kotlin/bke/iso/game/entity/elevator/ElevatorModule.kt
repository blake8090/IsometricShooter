package bke.iso.game.entity.elevator

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Event
import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.Module
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Properties
import bke.iso.engine.world.event.EntityCreated
import io.github.oshai.kotlinlogging.KotlinLogging

class ElevatorModule(
    private val collisions: Collisions,
    private val collisionBoxes: CollisionBoxes
) : Module {

    override val alwaysActive: Boolean = false

    private val log = KotlinLogging.logger { }

    override fun handleEvent(event: Event) {
        if (event is EntityCreated && event.entity.has<Elevator>()) {
            setupElevator(event.entity)
        }
    }

    private fun setupElevator(entity: Entity) {
        val box = checkNotNull(collisionBoxes[entity]) {
            "Expected collision box for $entity"
        }

        // push entity down to make sure it's completely flush with the ground
        entity.move(0f, 0f, box.size.z * -1f)

        log.debug { "Setup elevator $entity" }
    }

    fun canStartElevator(elevatorEntity: Entity): Boolean =
        !elevatorEntity.has<ElevatorTask>()

    fun startElevator(elevatorEntity: Entity) {
        if (!canStartElevator(elevatorEntity)) {
            return
        }

        var minZ = getFloatProperty(elevatorEntity, "min")
        if (minZ == null) {
            log.warn { "Cannot start elevator $elevatorEntity - tag 'min' not found!" }
            return
        }
        minZ -= collisionBoxes[elevatorEntity]!!.size.z

        var maxZ = getFloatProperty(elevatorEntity, "max")
        if (maxZ == null) {
            log.warn { "Cannot start elevator $elevatorEntity - tag 'max' not found!" }
            return
        }
        maxZ -= collisionBoxes[elevatorEntity]!!.size.z

        val elevator = checkNotNull(elevatorEntity.get<Elevator>()) {
            "Expected Elevator component for $elevatorEntity"
        }

        if (elevatorEntity.z > minZ) {
            elevatorEntity.add(ElevatorTask(minZ, ElevatorDirection.DOWN))
            elevatorEntity.with<PhysicsBody> { physicsBody ->
                physicsBody.velocity.z = elevator.speed * -1f
            }
        } else if (elevatorEntity.z < maxZ) {
            elevatorEntity.add(ElevatorTask(maxZ, ElevatorDirection.UP))
            elevatorEntity.with<PhysicsBody> { physicsBody ->
                physicsBody.velocity.z = elevator.speed
            }
        }
    }

    private fun getFloatProperty(entity: Entity, key: String): Float? {
        val property = entity.get<Properties>() ?: return null
        return property.values[key]?.toFloat()
    }

    fun findElevatorUnderneath(entity: Entity): Entity? =
        collisions
            .getCollisions(entity)
            .map { collision -> collision.entity }
            .firstOrNull { a -> a.has<Elevator>() }
}
