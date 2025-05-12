package bke.iso.game.actor.elevator

import bke.iso.engine.core.Event
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Module
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import bke.iso.engine.world.actor.Tags
import io.github.oshai.kotlinlogging.KotlinLogging

class ElevatorModule(private val collisions: Collisions) : Module {

    override val alwaysActive: Boolean = false

    private val log = KotlinLogging.logger { }

    override fun handleEvent(event: Event) {
        if (event is Actors.CreatedEvent && event.actor.has<Elevator>()) {
            setupElevator(event.actor)
        }
    }

    private fun setupElevator(actor: Actor) {
        val box = checkNotNull(actor.getCollisionBox()) {
            "Expected collision box for $actor"
        }

        // push actor down to make sure it's completely flush with the ground
        actor.move(0f, 0f, box.size.z * -1f)

        log.debug { "Setup elevator $actor" }
    }

    fun canStartElevator(elevatorActor: Actor): Boolean =
        !elevatorActor.has<ElevatorTask>()

    fun startElevator(elevatorActor: Actor) {
        if (!canStartElevator(elevatorActor)) {
            return
        }

        var minZ = getTagValue(elevatorActor, "min")
        if (minZ == null) {
            log.warn { "Cannot start elevator $elevatorActor - tag 'min' not found!" }
            return
        }
        minZ -= elevatorActor.getCollisionBox()!!.size.z

        var maxZ = getTagValue(elevatorActor, "max")
        if (maxZ == null) {
            log.warn { "Cannot start elevator $elevatorActor - tag 'max' not found!" }
            return
        }
        maxZ -= elevatorActor.getCollisionBox()!!.size.z

        val elevator = checkNotNull(elevatorActor.get<Elevator>()) {
            "Expected Elevator component for $elevatorActor"
        }

        if (elevatorActor.z > minZ) {
            elevatorActor.add(ElevatorTask(minZ, ElevatorDirection.DOWN))
            elevatorActor.with<PhysicsBody> { physicsBody ->
                physicsBody.velocity.z = elevator.speed * -1f
            }
        } else if (elevatorActor.z < maxZ) {
            elevatorActor.add(ElevatorTask(maxZ, ElevatorDirection.UP))
            elevatorActor.with<PhysicsBody> { physicsBody ->
                physicsBody.velocity.z = elevator.speed
            }
        }
    }

    private fun getTagValue(actor: Actor, prefix: String): Float? {
        return actor
            .get<Tags>()
            ?.tags
            ?.firstOrNull { tag -> tag.startsWith(prefix) }
            ?.substringAfter(":")
            ?.toFloat()
            ?: return null
    }

    fun findElevatorUnderneath(actor: Actor): Actor? =
        collisions
            .getCollisions(actor)
            .map { collision -> collision.actor }
            .firstOrNull { a -> a.has<Elevator>() }
}
