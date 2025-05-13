package bke.iso.game.occlusion

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.actor.ActorRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Tile
import bke.iso.game.actor.door.Door

class BuildingWallOcclusionStrategy(private val world: World) : OcclusionStrategy() {


    override fun firstPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
        if (targetRenderable == null) {
            return
        }

        val actor = checkNotNull(renderable.actor)
        val building = world.buildings.getBuilding(actor) ?: return
        val buildingBounds = world.buildings.getBounds(building) ?: return

        val targetBounds = checkNotNull(targetRenderable.bounds)
        if (targetBounds.intersects(buildingBounds) && isSouthOrEastWall(actor, buildingBounds)) {
            renderable.alpha = 0.1f
        }
    }

    private fun isSouthOrEastWall(actor: Actor, buildingBounds: Box): Boolean {
        if (actor.has<Tile>() || actor.has<Door>()) {
            return false
        }

        val box = actor.getCollisionBox() ?: return false
        return box.max.x >= buildingBounds.max.x || box.min.y <= buildingBounds.min.y
    }

    override fun secondPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {}

    override fun endFrame() {
    }
}
