package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor

/**
 * When the player enters a building, hides walls on the south and east sides so that the player is not obscured.
 */
class PlayerBuildingVisibilitySystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        val playerActor = world.actors.find<Player>() ?: return

        for (buildingName in world.buildings.getAll()) {
            checkVisibility(playerActor, buildingName)
        }
    }

    private fun checkVisibility(playerActor: Actor, buildingName: String) {
        val bounds = world.buildings.getBounds(buildingName) ?: return
        val box = playerActor.getCollisionBox() ?: return

        if (bounds.intersects(box)) {
            hideWalls(buildingName, bounds)
        } else {
            showWalls(buildingName, bounds)
        }
    }

    private fun hideWalls(buildingName: String, bounds: Box) {
        // TODO: add a wall component and filter actors
        val walls = world.buildings
            .getAllObjects(buildingName)
            .filterIsInstance<Actor>()
            .filter { wall -> isSouthOrEastWall(wall, bounds) }

        // TODO: add a sprite alpha override component?
        for (wall in walls) {
            wall.with<Sprite> { sprite -> sprite.alpha = 0.2f }
        }
    }

    private fun showWalls(buildingName: String, bounds: Box) {
        val walls = world.buildings
            .getAllObjects(buildingName)
            .filterIsInstance<Actor>()
            .filter { wall -> isSouthOrEastWall(wall, bounds) }

        for (wall in walls) {
            wall.with<Sprite> { sprite -> sprite.alpha = 1f }
        }
    }

    private fun isSouthOrEastWall(actor: Actor, buildingBounds: Box): Boolean {
        val box = actor.getCollisionBox() ?: return false
        return box.max.x >= buildingBounds.max.x || box.min.y <= buildingBounds.min.y
    }
}
