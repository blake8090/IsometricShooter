package bke.iso.engine.render.occlusion

import bke.iso.engine.render.entity.EntityRenderable
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import kotlin.reflect.KClass

class Occlusion(private val world: World) {

    var target: Entity? = null
    private var targetRenderable: EntityRenderable? = null

    private val strategies = mutableListOf<OcclusionStrategy>()

    init {
        resetStrategies()
    }

    fun addStrategy(occlusionStrategy: OcclusionStrategy) {
        strategies.add(occlusionStrategy)
    }

    fun prepare(renderable: EntityRenderable) {
        if (renderable.entity == target) {
            targetRenderable = renderable
        }
    }

    fun firstPass(renderable: EntityRenderable) {
        for (strategy in strategies) {
            strategy.firstPass(renderable, targetRenderable)
        }
    }

    fun secondPass(renderable: EntityRenderable) {
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
