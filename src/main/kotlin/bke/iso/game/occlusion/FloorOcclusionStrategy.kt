package bke.iso.game.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.Tile
import bke.iso.engine.world.actor.has
import bke.iso.game.actor.elevator.Elevator
import kotlin.math.floor

class FloorOcclusionStrategy(private val floorHeight: Float) : OcclusionStrategy() {

    private var minimumLayer: Float? = null

    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
        if (targetRenderable == null) {
            return
        }

        val targetFloor = getFloor(targetRenderable)
        val floor = getFloor(renderable)
        if (floor != targetFloor) {
            renderable.alpha = 0f
        }
    }

    private fun getFloor(renderable: GameObjectRenderable): Float {
        val gameObject = checkNotNull(renderable.gameObject)
        val bounds = checkNotNull(renderable.bounds)

        return if (renderable.gameObject is Tile) {
            floor(bounds.min.z / floorHeight)
        } else if (gameObject.has<Elevator>()) {
            // elevators are always positioned so that the top of the collision box is flush with the ground
            floor(bounds.max.z / floorHeight)
        } else {
            floor(bounds.min.z / floorHeight)
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {}

    override fun endFrame() {
        minimumLayer = null
    }
}
