package bke.iso.game.entity.elevator

import bke.iso.engine.state.System
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity

class ElevatorSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.entities.each<ElevatorTask> { actor, elevatorTask ->
            update(actor, elevatorTask)
        }
    }

    private fun update(entity: Entity, elevatorTask: ElevatorTask) {
        if (elevatorTask.direction == ElevatorDirection.DOWN && entity.z <= elevatorTask.targetZ) {
            stopElevator(entity, elevatorTask)
        } else if (elevatorTask.direction == ElevatorDirection.UP && entity.z >= elevatorTask.targetZ) {
            stopElevator(entity, elevatorTask)
        }
    }

    private fun stopElevator(entity: Entity, elevatorTask: ElevatorTask) {
        entity.moveTo(entity.x, entity.y, elevatorTask.targetZ)
        entity.remove<ElevatorTask>()
        entity.with<PhysicsBody> { physicsBody -> physicsBody.velocity.z = 0f }
    }
}
