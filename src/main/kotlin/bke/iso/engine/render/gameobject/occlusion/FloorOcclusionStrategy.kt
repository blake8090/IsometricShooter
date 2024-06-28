package bke.iso.engine.render.gameobject.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.world.Tile
import bke.iso.engine.world.actor.Actor
import bke.iso.game.elevator.Elevator
import kotlin.math.floor

class FloorOcclusionStrategy : OcclusionStrategy() {

    private var minimumLayer: Float? = null

    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable) {
        val targetFloor = getFloor(targetRenderable)
        val floor = getFloor(renderable)
        if (floor != targetFloor) {
            renderable.alpha = 0f
        }
    }

    private fun getFloor(renderable: GameObjectRenderable): Float {
        val bounds = checkNotNull(renderable.bounds)

        return if (renderable.gameObject is Tile) {
            floor(bounds.min.z / 2f)
        } else if (renderable.gameObject is Actor && (renderable.gameObject as Actor).has<Elevator>()) {
            // elevators are always positioned so that the top of the collision box is flush with the ground
            floor(bounds.max.z / 2f)
        } else {
            floor(bounds.min.z / 2f)
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable) {
    }

    override fun endFrame() {
        minimumLayer = null
    }
}
