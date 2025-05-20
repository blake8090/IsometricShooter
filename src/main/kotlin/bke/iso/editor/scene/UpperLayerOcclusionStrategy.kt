package bke.iso.editor.scene

import bke.iso.engine.render.entity.EntityRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy

class UpperLayerOcclusionStrategy(private val sceneEditor: SceneEditor) : OcclusionStrategy() {
    override fun firstPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {
        val entity = checkNotNull(renderable.entity)

        if (entity.z > sceneEditor.selectedLayer && sceneEditor.hideUpperLayers) {
            if (entity.has<EntityTemplateReference>() || entity.has<TileTemplateReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: EntityRenderable, targetRenderable: EntityRenderable?) {}

    override fun endFrame() {}
}
