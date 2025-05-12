package bke.iso.engine.render.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.world.World
import kotlin.math.floor

class BuildingLayerOcclusionStrategy(private val world: World) : OcclusionStrategy() {

    private val minimumLayerByBuilding = mutableMapOf<String, Float>()

    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
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

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
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
