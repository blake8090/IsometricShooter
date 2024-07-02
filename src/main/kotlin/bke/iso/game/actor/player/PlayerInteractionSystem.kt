package bke.iso.game.actor.player

import bke.iso.engine.state.System
import bke.iso.engine.input.Input
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.actor.door.DoorChangeSceneAction
import bke.iso.game.actor.door.DoorModule
import bke.iso.game.actor.door.DoorOpenAction
import bke.iso.game.actor.elevator.ElevatorModule
import bke.iso.game.hud.HudModule

class PlayerInteractionSystem(
    private val world: World,
    private val input: Input,
    private val hudModule: HudModule,
    private val doorModule: DoorModule,
    private val elevatorModule: ElevatorModule
) : System {
    override fun update(deltaTime: Float) {
        world.actors.each<Player> { actor, _ ->
            update(actor)
        }
    }

    private fun update(actor: Actor) {
        hudModule.hideInteractionText()

        val doorActor = doorModule.getNearestDoor(actor.pos, PLAYER_DOOR_ACTION_RADIUS)
        val elevatorActor = elevatorModule.findElevatorUnderneath(actor)

        if (doorActor != null) {
            handleDoor(actor, doorActor)
        } else if (elevatorActor != null) {
            handleElevator(elevatorActor)
        }
    }

    private fun handleDoor(playerActor: Actor, doorActor: Actor) {
        if (doorActor.has<DoorOpenAction>()) {
            hudModule.setInteractionText("Press E or Y to open")
        } else if (doorActor.has<DoorChangeSceneAction>()) {
            hudModule.setInteractionText("Press E or Y to enter")
        }

        input.onAction("interact") {
            doorModule.openDoor(playerActor, doorActor)
        }
    }

    private fun handleElevator(elevatorActor: Actor) {
        if (elevatorModule.canStartElevator(elevatorActor)) {
            hudModule.setInteractionText("Press E or Y to ride")
        }

        input.onAction("interact") {
            elevatorModule.startElevator(elevatorActor)
        }
    }
}
