package bke.iso.editorv2.scene.layer

import bke.iso.editorv2.scene.ActorPrefabReference
import bke.iso.editorv2.scene.TilePrefabReference
import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.render.occlusion.OcclusionStrategy
import bke.iso.engine.world.actor.Actor

class UpperLayerOcclusionStrategy2(private val layerModule: LayerModule2) : OcclusionStrategy() {

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
