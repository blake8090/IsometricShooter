package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.door.Door

/**
 * When the player enters a building, hides walls on the south and east sides so that the player is not obscured.
 */
// TODO: make this an occlusion strategy
class PlayerBuildingVisibilitySystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        val playerActor = world.actors.find<Player>() ?: return

        for (buildingName in world.buildings.getAll()) {
            updateWallVisibility(playerActor, buildingName)
        }
    }

    private fun updateWallVisibility(playerActor: Actor, buildingName: String) {
        val bounds = world.buildings.getBounds(buildingName) ?: return
        val box = playerActor.getCollisionBox() ?: return

        val alpha =
            if (bounds.intersects(box)) {
                0.1f
            } else {
                1f
            }

        for (wall in getSouthAndEastWalls(buildingName, bounds)) {
            // TODO: add a sprite alpha override component?
            wall.with<Sprite> { sprite ->
                sprite.alpha = alpha
            }
        }
    }

    private fun getSouthAndEastWalls(buildingName: String, bounds: Box) =
        world.buildings
            .getAllObjects(buildingName)
            .filterIsInstance<Actor>()
            .filter { wall ->
                isSouthOrEastWall(wall, bounds) && !wall.has<Door>()
            }

    private fun isSouthOrEastWall(actor: Actor, buildingBounds: Box): Boolean {
        val box = actor.getCollisionBox() ?: return false
        return box.max.x >= buildingBounds.max.x || box.min.y <= buildingBounds.min.y
    }
}
