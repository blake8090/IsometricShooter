package bke.iso.game.occlusion

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.entity.EntityRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tile
import bke.iso.game.entity.door.Door

class BuildingWallOcclusionStrategy(private val world: World) : OcclusionStrategy() {


    override fun firstPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        if (targetRenderable == null) {
            return
        }

        val actor = checkNotNull(renderable.entity)
        val building = world.buildings.getBuilding(actor) ?: return
        val buildingBounds = world.buildings.getBounds(building) ?: return

        val targetBounds = checkNotNull(targetRenderable.bounds)
        if (targetBounds.intersects(buildingBounds) && isSouthOrEastWall(actor, buildingBounds)) {
            renderable.alpha = 0.1f
        }
    }

    private fun isSouthOrEastWall(entity: Entity, buildingBounds: Box): Boolean {
        if (entity.has<Tile>() || entity.has<Door>()) {
            return false
        }

        val box = entity.getCollisionBox() ?: return false
        return box.max.x >= buildingBounds.max.x || box.min.y <= buildingBounds.min.y
    }

    override fun secondPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {}

    override fun endFrame() {
    }
}
