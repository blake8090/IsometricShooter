package bke.iso.engine.render.occlusion

import bke.iso.engine.render.entity.EntityRenderable

class BasicOcclusionStrategy : OcclusionStrategy() {

    override fun firstPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        if (targetRenderable != null && occludes(renderable, targetRenderable)) {
            renderable.alpha = 0.1f
        }
    }

    override fun secondPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
    }

    override fun endFrame() {
    }
}
