package bke.iso.engine.render.occlusion

import bke.iso.engine.render.actor.ActorRenderable
import bke.iso.engine.world.World
import kotlin.math.floor

class BuildingLayerOcclusionStrategy(private val world: World) : OcclusionStrategy() {

    private val minimumLayerByBuilding = mutableMapOf<String, Float>()

    override fun firstPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
        if (targetRenderable == null || !occludes(renderable, targetRenderable)) {
            return
        }

        val actor = checkNotNull(renderable.actor)
        val building = world.buildings.getBuilding(actor) ?: return
        val bounds = checkNotNull(renderable.bounds)

        val layer = floor(bounds.min.z)
        val minimumLayer = minimumLayerByBuilding[building]
        if (minimumLayer == null || layer < minimumLayer) {
            minimumLayerByBuilding[building] = layer
        }
    }

    override fun secondPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
        val actor = checkNotNull(renderable.actor)
        val building = world.buildings.getBuilding(actor) ?: return
        val bounds = checkNotNull(renderable.bounds)

        val minimumLayer = minimumLayerByBuilding[building] ?: return
        if (floor(bounds.min.z) > minimumLayer) {
            renderable.alpha = 0f
        }
    }

    override fun endFrame() {
        minimumLayerByBuilding.clear()
    }
}
