package bke.iso.editor.scene.layer

import bke.iso.editor.scene.ActorPrefabReference
import bke.iso.editor.scene.TilePrefabReference
import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.actor.Actor

class UpperLayerOcclusionStrategy(private val layerModule: LayerModule) : OcclusionStrategy() {

    override fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {
        val gameObject = checkNotNull(renderable.gameObject)

        if (gameObject is Actor && gameObject.z > layerModule.selectedLayer && layerModule.hideUpperLayers) {
            if (gameObject.has<ActorPrefabReference>() || gameObject.has<TilePrefabReference>()) {
                renderable.alpha = 0f
            }
        }
    }

    override fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?) {}

    override fun endFrame() {}
}
