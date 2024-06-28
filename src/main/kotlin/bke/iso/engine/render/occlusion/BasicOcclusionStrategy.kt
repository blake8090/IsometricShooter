package bke.iso.engine.render.occlusion

import bke.iso.engine.render.gameobject.GameObjectRenderable

class BasicOcclusionStrategy : OcclusionStrategy() {

    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
        if (targetRenderable != null && occludes(renderable, targetRenderable)) {
            renderable.alpha = 0.1f
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
    }

    override fun endFrame() {
    }
}
