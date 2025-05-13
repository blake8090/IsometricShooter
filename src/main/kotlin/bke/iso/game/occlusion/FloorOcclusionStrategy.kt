package bke.iso.game.occlusion

import bke.iso.engine.render.actor.ActorRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.entity.Tile
import bke.iso.game.actor.elevator.Elevator
import kotlin.math.floor

class FloorOcclusionStrategy(private val floorHeight: Float) : OcclusionStrategy() {

    private var minimumLayer: Float? = null

    override fun firstPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
        if (targetRenderable == null) {
            return
        }

        val targetFloor = getFloor(targetRenderable)
        val floor = getFloor(renderable)
        if (floor != targetFloor) {
            renderable.alpha = 0f
        }
    }

    private fun getFloor(renderable: ActorRenderable): Float {
        val actor = checkNotNull(renderable.actor)
        val bounds = checkNotNull(renderable.bounds)

        return if (renderable.actor!!.has<Tile>()) {
            floor(bounds.min.z / floorHeight)
        } else if (actor.has<Elevator>()) {
            // elevators are always positioned so that the top of the collision box is flush with the ground
            floor(bounds.max.z / floorHeight)
        } else {
            floor(bounds.min.z / floorHeight)
        }
    }

    override fun secondPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {}

    override fun endFrame() {
        minimumLayer = null
    }
}
