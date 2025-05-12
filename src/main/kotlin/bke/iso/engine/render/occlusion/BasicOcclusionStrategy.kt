package bke.iso.engine.render.occlusion

import bke.iso.engine.render.actor.ActorRenderable

class BasicOcclusionStrategy : OcclusionStrategy() {

    override fun firstPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
        if (targetRenderable != null && occludes(renderable, targetRenderable)) {
            renderable.alpha = 0.1f
        }
    }

    override fun secondPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
    }

    override fun endFrame() {
    }
}
