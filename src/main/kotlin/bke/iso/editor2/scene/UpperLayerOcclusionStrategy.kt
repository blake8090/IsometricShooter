package bke.iso.editor2.scene

import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.actor.Actor

class UpperLayerOcclusionStrategy(private val sceneMode: SceneMode) : OcclusionStrategy() {
    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
        val gameObject = checkNotNull(renderable.gameObject)

        if (gameObject is Actor && gameObject.z > sceneMode.selectedLayer && sceneMode.hideUpperLayers) {
            if (gameObject.has<ActorPrefabReference>() || gameObject.has<TilePrefabReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {}

    override fun endFrame() {}
}
