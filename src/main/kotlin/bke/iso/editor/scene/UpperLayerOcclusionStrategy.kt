package bke.iso.editor.scene

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy

class UpperLayerOcclusionStrategy(private val sceneMode: SceneMode) : OcclusionStrategy() {
    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
        val actor = checkNotNull(renderable.actor)

        if (actor.z > sceneMode.selectedLayer && sceneMode.hideUpperLayers) {
            if (actor.has<ActorPrefabReference>() || actor.has<TilePrefabReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {}

    override fun endFrame() {}
}
