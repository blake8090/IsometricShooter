package bke.iso.game.door

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.ui.UI
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.GameState
import bke.iso.game.player.Player
import com.badlogic.gdx.math.Vector3

class DoorModule(private val world: World, private val ui: UI, private val events: Game.Events) : Module {

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        if (event is OpenDoorEvent) {
            openDoor(event.actor, event.doorActor)
        }
    }

    fun getNearestDoor(pos: Vector3, range: Float): Actor? {
        for (actor in world.actors.findAll<Door>()) {
            if (actor.pos.dst(pos) <= range) {
                return actor
            }
        }
        return null
    }

    fun openDoor(actor: Actor, doorActor: Actor) {
        val changeSceneAction = doorActor.get<DoorChangeSceneAction>()
        if (changeSceneAction != null && actor.has<Player>()) {
            ui.loadingScreen.start {
                events.fire(GameState.LoadSceneEvent(changeSceneAction.sceneName))
            }
        } else if (doorActor.has<DoorOpenAction>()) {
            println("opening door")
        }
    }

    data class OpenDoorEvent(
        val actor: Actor,
        val doorActor: Actor
    ) : Event
}
