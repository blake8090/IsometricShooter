package bke.iso.engine.render.gameobject.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor

class Occlusion(world: World) {

    var target: Actor? = null
    private var targetRenderable: GameObjectRenderable? = null

    private val strategies = mutableListOf<OcclusionStrategy>()

    init {
        strategies.add(BasicOcclusionStrategy())
        strategies.add(BuildingLayerOcclusionStrategy(world))
        strategies.add(FloorOcclusionStrategy())
    }

    fun prepare(renderable: GameObjectRenderable) {
        if (renderable.gameObject == target) {
            targetRenderable = renderable
        }
    }

    fun firstPass(renderable: GameObjectRenderable) {
        val t = targetRenderable ?: return
        for (strategy in strategies) {
            strategy.firstPass(renderable, t)
        }
    }

    fun secondPass(renderable: GameObjectRenderable) {
        val t = targetRenderable ?: return
        for (strategy in strategies) {
            strategy.secondPass(renderable, t)
        }
    }

    fun endFrame() {
        for (strategy in strategies) {
            strategy.endFrame()
        }
    }

    fun resetStrategies() {
        strategies.clear()
        strategies.add(BasicOcclusionStrategy())
    }
}
