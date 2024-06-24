package bke.iso.engine.render.gameobject.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.world.World
import kotlin.math.floor

class BuildingLayerOcclusionStrategy(private val world: World) : OcclusionStrategy() {

    private val minimumVisibleLayers = mutableMapOf<String, Float>()

    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable) {
        if (!occludes(renderable, targetRenderable)) {
            return
        }

        val bounds = checkNotNull(renderable.bounds)
        val gameObject = checkNotNull(renderable.gameObject)
        val targetBounds = checkNotNull(targetRenderable.bounds)

        val building = world.buildings.getBuilding(gameObject) ?: return

        if (bounds.min.z >= targetBounds.max.z) {
            val layer = floor(bounds.min.z)
            val minimumLayer = minimumVisibleLayers[building]
            if (minimumLayer == null || layer < minimumLayer) {
                minimumVisibleLayers[building] = layer
            }
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable) {
        val bounds = checkNotNull(renderable.bounds)
        val gameObject = checkNotNull(renderable.gameObject)
        val building = world.buildings.getBuilding(gameObject) ?: return

        val minimumLayer = minimumVisibleLayers[building] ?: return
        if (floor(bounds.min.z) >= minimumLayer) {
            renderable.alpha = 0f
        }
    }

    override fun endFrame() {
        minimumVisibleLayers.clear()
    }
}
