package bke.iso.game.actor.elevator

import bke.iso.engine.state.System
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Actor

class ElevatorSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actors.each<ElevatorTask> { actor, elevatorTask ->
            update(actor, elevatorTask)
        }
    }

    private fun update(actor: Actor, elevatorTask: ElevatorTask) {
        if (elevatorTask.direction == ElevatorDirection.DOWN && actor.z <= elevatorTask.targetZ) {
            stopElevator(actor, elevatorTask)
        } else if (elevatorTask.direction == ElevatorDirection.UP && actor.z >= elevatorTask.targetZ) {
            stopElevator(actor, elevatorTask)
        }
    }

    private fun stopElevator(actor: Actor, elevatorTask: ElevatorTask) {
        actor.moveTo(actor.x, actor.y, elevatorTask.targetZ)
        actor.remove<ElevatorTask>()
        actor.with<PhysicsBody> { physicsBody -> physicsBody.velocity.z = 0f }
    }
}
