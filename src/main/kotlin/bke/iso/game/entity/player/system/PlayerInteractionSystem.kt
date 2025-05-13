package bke.iso.game.entity.player.system

import bke.iso.engine.state.System
import bke.iso.engine.input.Input
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.game.entity.door.DoorChangeSceneAction
import bke.iso.game.entity.door.DoorModule
import bke.iso.game.entity.door.DoorOpenAction
import bke.iso.game.entity.elevator.ElevatorModule
import bke.iso.game.entity.player.Player
import bke.iso.game.hud.HudModule

private const val PLAYER_DOOR_ACTION_RADIUS = 1.2f

class PlayerInteractionSystem(
    private val world: World,
    private val input: Input,
    private val hudModule: HudModule,
    private val doorModule: DoorModule,
    private val elevatorModule: ElevatorModule
) : System {
    override fun update(deltaTime: Float) {
        world.entities.each<Player> { actor, _ ->
            update(actor)
        }
    }

    private fun update(entity: Entity) {
        hudModule.hideInteractionText()

        val doorActor = doorModule.getNearestDoor(entity.pos, PLAYER_DOOR_ACTION_RADIUS)
        val elevatorActor = elevatorModule.findElevatorUnderneath(entity)

        if (doorActor != null) {
            handleDoor(entity, doorActor)
        } else if (elevatorActor != null) {
            handleElevator(elevatorActor)
        }
    }

    private fun handleDoor(playerEntity: Entity, doorEntity: Entity) {
        if (doorEntity.has<DoorOpenAction>()) {
            hudModule.setInteractionText("Press E or Y to open")
        } else if (doorEntity.has<DoorChangeSceneAction>()) {
            hudModule.setInteractionText("Press E or Y to enter")
        }

        input.onAction("interact") {
            doorModule.openDoor(playerEntity, doorEntity)
        }
    }

    private fun handleElevator(elevatorEntity: Entity) {
        if (elevatorModule.canStartElevator(elevatorEntity)) {
            hudModule.setInteractionText("Press E or Y to ride")
        }

        input.onAction("interact") {
            elevatorModule.startElevator(elevatorEntity)
        }
    }
}
