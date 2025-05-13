package bke.iso.editor.scene

import bke.iso.engine.render.entity.EntityRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy

class UpperLayerOcclusionStrategy(private val sceneMode: SceneMode) : OcclusionStrategy() {
    override fun firstPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        val entity = checkNotNull(renderable.entity)

        if (entity.z > sceneMode.selectedLayer && sceneMode.hideUpperLayers) {
            if (entity.has<EntityPrefabReference>() || entity.has<TilePrefabReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {}

    override fun endFrame() {}
}
