package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.input.Input
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.actor.MovingPlatform
import bke.iso.game.door.DoorChangeSceneAction
import bke.iso.game.door.DoorModule
import bke.iso.game.door.DoorOpenAction
import bke.iso.game.hud.HudModule
import com.badlogic.gdx.math.Vector3

class PlayerInteractionSystem(
    private val world: World,
    private val input: Input,
    private val hudModule: HudModule,
    private val doorModule: DoorModule
) : System {
    override fun update(deltaTime: Float) {
        world.actors.each<Player> { actor, _ ->
            update(actor)
        }
    }

    private fun update(actor: Actor) {
        hudModule.hideInteractionText()

        val doorActor = doorModule.getNearestDoor(actor.pos, PLAYER_DOOR_ACTION_RADIUS)
        val platform = getNearestPlatform(actor.pos, PLAYER_DOOR_ACTION_RADIUS)

        if (doorActor != null) {
            handleDoor(actor, doorActor)
        } else if (platform != null) {
            hudModule.setInteractionText("Press E or Y to ride")
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

    private fun getNearestPlatform(pos: Vector3, range: Float): Actor? {
        for (actor in world.actors.findAll<MovingPlatform>()) {
            if (actor.pos.dst(pos) <= range) {
                return actor
            }
        }
        return null
    }
}
