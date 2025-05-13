package bke.iso.engine.render.occlusion

import bke.iso.engine.render.entity.EntityRenderable
import bke.iso.engine.world.World
import kotlin.math.floor

class BuildingLayerOcclusionStrategy(private val world: World) : OcclusionStrategy() {

    private val minimumLayerByBuilding = mutableMapOf<String, Float>()

    override fun firstPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        if (targetRenderable == null || !occludes(renderable, targetRenderable)) {
            return
        }

        val entity = checkNotNull(renderable.entity)
        val building = world.buildings.getBuilding(entity) ?: return
        val bounds = checkNotNull(renderable.bounds)

        val layer = floor(bounds.min.z)
        val minimumLayer = minimumLayerByBuilding[building]
        if (minimumLayer == null || layer < minimumLayer) {
            minimumLayerByBuilding[building] = layer
        }
    }

    override fun secondPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        val entity = checkNotNull(renderable.entity)
        val building = world.buildings.getBuilding(entity) ?: return
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
