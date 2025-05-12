package bke.iso.engine.render.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import kotlin.reflect.KClass

class Occlusion(private val world: World) {

    var target: Actor? = null
    private var targetRenderable: GameObjectRenderable? = null

    private val strategies = mutableListOf<OcclusionStrategy>()

    init {
        resetStrategies()
    }

    fun addStrategy(occlusionStrategy: OcclusionStrategy) {
        strategies.add(occlusionStrategy)
    }

    fun prepare(renderable: GameObjectRenderable) {
        if (renderable.actor == target) {
            targetRenderable = renderable
        }
    }

    fun firstPass(renderable: GameObjectRenderable) {
        for (strategy in strategies) {
            strategy.firstPass(renderable, targetRenderable)
        }
    }

    fun secondPass(renderable: GameObjectRenderable) {
        for (strategy in strategies) {
            strategy.secondPass(renderable, targetRenderable)
        }
    }

    fun endFrame() {
        for (strategy in strategies) {
            strategy.endFrame()
        }
        targetRenderable = null
    }

    fun resetStrategies() {
        strategies.clear()
        strategies.add(BasicOcclusionStrategy())
        strategies.add(BuildingLayerOcclusionStrategy(world))
    }

    fun <T : OcclusionStrategy> removeStrategy(type: KClass<T>) {
        strategies.removeIf { strategy -> type.isInstance(strategy) }
    }
}
