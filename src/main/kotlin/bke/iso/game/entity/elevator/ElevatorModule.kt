package bke.iso.game.entity.elevator

import bke.iso.engine.core.Event
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Module
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Entities
import bke.iso.engine.world.entity.Tags
import io.github.oshai.kotlinlogging.KotlinLogging

class ElevatorModule(private val collisions: Collisions) : Module {

    override val alwaysActive: Boolean = false

    private val log = KotlinLogging.logger { }

    override fun handleEvent(event: Event) {
        if (event is Entities.CreatedEvent && event.entity.has<Elevator>()) {
            setupElevator(event.entity)
        }
    }

    private fun setupElevator(entity: Entity) {
        val box = checkNotNull(entity.getCollisionBox()) {
            "Expected collision box for $entity"
        }

        // push actor down to make sure it's completely flush with the ground
        entity.move(0f, 0f, box.size.z * -1f)

        log.debug { "Setup elevator $entity" }
    }

    fun canStartElevator(elevatorEntity: Entity): Boolean =
        !elevatorEntity.has<ElevatorTask>()

    fun startElevator(elevatorEntity: Entity) {
        if (!canStartElevator(elevatorEntity)) {
            return
        }

        var minZ = getTagValue(elevatorEntity, "min")
        if (minZ == null) {
            log.warn { "Cannot start elevator $elevatorEntity - tag 'min' not found!" }
            return
        }
        minZ -= elevatorEntity.getCollisionBox()!!.size.z

        var maxZ = getTagValue(elevatorEntity, "max")
        if (maxZ == null) {
            log.warn { "Cannot start elevator $elevatorEntity - tag 'max' not found!" }
            return
        }
        maxZ -= elevatorEntity.getCollisionBox()!!.size.z

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

    private fun getTagValue(entity: Entity, prefix: String): Float? {
        return entity
            .get<Tags>()
            ?.tags
            ?.firstOrNull { tag -> tag.startsWith(prefix) }
            ?.substringAfter(":")
            ?.toFloat()
            ?: return null
    }

    fun findElevatorUnderneath(entity: Entity): Entity? =
        collisions
            .getCollisions(entity)
            .map { collision -> collision.entity }
            .firstOrNull { a -> a.has<Elevator>() }
}
