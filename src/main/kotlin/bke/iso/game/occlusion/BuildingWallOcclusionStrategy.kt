package bke.iso.game.occlusion

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.has
import bke.iso.game.actor.door.Door

class BuildingWallOcclusionStrategy(private val world: World) : OcclusionStrategy() {


    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
        if (targetRenderable == null) {
            return
        }

        val gameObject = checkNotNull(renderable.gameObject)
        val building = world.buildings.getBuilding(gameObject) ?: return
        val buildingBounds = world.buildings.getBounds(building) ?: return

        val targetBounds = checkNotNull(targetRenderable.bounds)
        if (targetBounds.intersects(buildingBounds) && isSouthOrEastWall(gameObject, buildingBounds)) {
            renderable.alpha = 0.1f
        }
    }

    private fun isSouthOrEastWall(gameObject: GameObject, buildingBounds: Box): Boolean {
        if (gameObject is Tile || gameObject.has<Door>()) {
            return false
        }

        val box = gameObject.getCollisionBox() ?: return false
        return box.max.x >= buildingBounds.max.x || box.min.y <= buildingBounds.min.y
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {}

    override fun endFrame() {
    }
}
