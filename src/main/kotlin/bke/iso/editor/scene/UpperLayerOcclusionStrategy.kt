package bke.iso.editor.scene

import bke.iso.engine.render.entity.EntityRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy

class UpperLayerOcclusionStrategy(private val sceneMode: SceneMode) : OcclusionStrategy() {
    override fun firstPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        val actor = checkNotNull(renderable.entity)

        if (actor.z > sceneMode.selectedLayer && sceneMode.hideUpperLayers) {
            if (actor.has<EntityPrefabReference>() || actor.has<TilePrefabReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {}

    override fun endFrame() {}
}
