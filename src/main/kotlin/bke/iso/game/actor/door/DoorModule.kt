package bke.iso.game.actor.door

import bke.iso.engine.Event
import bke.iso.engine.Events
import bke.iso.engine.state.Module
import bke.iso.engine.ui.UI
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import bke.iso.engine.world.actor.Tags
import bke.iso.game.GameState
import bke.iso.game.actor.player.Player
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class DoorModule(
    private val world: World,
    private val ui: UI,
    private val events: Events
) : Module {

    private val log = KotlinLogging.logger { }

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        if (event is OpenDoorEvent) {
            openDoor(event.actor, event.doorActor)
        } else if (event is Actors.CreatedEvent) {
            if (event.actor.has<Door>()) {
                setupDoor(event.actor)
            }
        }
    }

    private fun setupDoor(actor: Actor) {
        log.debug { "setting up door $actor" }

        val sceneTag = findSceneTag(actor)
        if (sceneTag != null) {
            val sceneName = sceneTag.substringAfter(":")
            actor.add(DoorChangeSceneAction(sceneName))
            log.debug { "set up door $actor with action: change scene to '$sceneName'" }
        } else {
            actor.add(DoorOpenAction())
            log.debug { "set up door $actor with action: open" }
        }
    }

    private fun findSceneTag(actor: Actor): String? {
        val tags = actor.get<Tags>() ?: return null

        return tags.tags.firstOrNull { tag ->
            tag.startsWith("scene")
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
                events.fire(GameState.LoadSceneEvent(changeSceneAction.sceneName, true))
            }
        } else if (doorActor.has<DoorOpenAction>()) {
            println("opening door")
            world.delete(doorActor)
        }
    }

    data class OpenDoorEvent(
        val actor: Actor,
        val doorActor: Actor
    ) : Event
}
