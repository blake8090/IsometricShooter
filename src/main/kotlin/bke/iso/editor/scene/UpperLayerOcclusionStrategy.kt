package bke.iso.editor.scene

import bke.iso.engine.render.actor.ActorRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy

class UpperLayerOcclusionStrategy(private val sceneMode: SceneMode) : OcclusionStrategy() {
    override fun firstPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {
        val actor = checkNotNull(renderable.actor)

        if (actor.z > sceneMode.selectedLayer && sceneMode.hideUpperLayers) {
            if (actor.has<ActorPrefabReference>() || actor.has<TilePrefabReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?) {}

    override fun endFrame() {}
}
