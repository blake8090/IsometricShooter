package bke.iso.game.actor.door

import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.core.Module
import bke.iso.engine.loading.SimpleLoadingScreen
import bke.iso.engine.loading.LoadingScreens
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Actor
import bke.iso.engine.world.entity.Actors
import bke.iso.engine.world.entity.Tags
import bke.iso.game.GameState
import bke.iso.game.actor.player.Player
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class DoorModule(
    private val world: World,
    private val loadingScreens: LoadingScreens,
    private val events: Events,
    private val assets: Assets
) : Module {

    override val alwaysActive: Boolean = false

    private val log = KotlinLogging.logger { }

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
            loadingScreens.start(SimpleLoadingScreen(assets)) {
                events.fire(GameState.LoadSceneEvent(changeSceneAction.sceneName, true))
            }
        } else if (doorActor.has<DoorOpenAction>()) {
            world.delete(doorActor)
        }
    }

    data class OpenDoorEvent(
        val actor: Actor,
        val doorActor: Actor
    ) : Event
}
