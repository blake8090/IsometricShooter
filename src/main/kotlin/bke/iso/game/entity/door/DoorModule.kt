package bke.iso.game.entity.door

import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.core.Module
import bke.iso.engine.loading.SimpleLoadingScreen
import bke.iso.engine.loading.LoadingScreens
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Entities
import bke.iso.engine.world.entity.Tags
import bke.iso.game.GameState
import bke.iso.game.entity.player.Player
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
            openDoor(event.entity, event.doorEntity)
        } else if (event is Entities.CreatedEvent) {
            if (event.entity.has<Door>()) {
                setupDoor(event.entity)
            }
        }
    }

    private fun setupDoor(entity: Entity) {
        log.debug { "setting up door $entity" }

        val sceneTag = findSceneTag(entity)
        if (sceneTag != null) {
            val sceneName = sceneTag.substringAfter(":")
            entity.add(DoorChangeSceneAction(sceneName))
            log.debug { "set up door $entity with action: change scene to '$sceneName'" }
        } else {
            entity.add(DoorOpenAction())
            log.debug { "set up door $entity with action: open" }
        }
    }

    private fun findSceneTag(entity: Entity): String? {
        val tags = entity.get<Tags>() ?: return null

        return tags.tags.firstOrNull { tag ->
            tag.startsWith("scene")
        }
    }

    fun getNearestDoor(pos: Vector3, range: Float): Entity? {
        for (actor in world.entities.findAll<Door>()) {
            if (actor.pos.dst(pos) <= range) {
                return actor
            }
        }
        return null
    }

    fun openDoor(entity: Entity, doorEntity: Entity) {
        val changeSceneAction = doorEntity.get<DoorChangeSceneAction>()
        if (changeSceneAction != null && entity.has<Player>()) {
            loadingScreens.start(SimpleLoadingScreen(assets)) {
                events.fire(GameState.LoadSceneEvent(changeSceneAction.sceneName, true))
            }
        } else if (doorEntity.has<DoorOpenAction>()) {
            world.delete(doorEntity)
        }
    }

    data class OpenDoorEvent(
        val entity: Entity,
        val doorEntity: Entity
    ) : Event
}
