package bke.iso.editorv2.scene

import bke.iso.editorv2.scene.camera.CameraModule2
import bke.iso.editorv2.scene.layer.LayerModule2
import bke.iso.editorv2.scene.ui.SceneTabView
import bke.iso.engine.Game

class SceneTabViewController(
    game: Game,
    sceneTabView: SceneTabView
) {

    private val referenceActorModule = ReferenceActorModule(game.world)

    private val sceneModule = SceneModule(
        game.dialogs,
        game.serializer,
        game.world,
        game.assets,
        referenceActorModule
    )

    private val layerModule = LayerModule2(
        sceneTabView,
        game.world,
        game.events,
        game.renderer
    )

    private val cameraModule = CameraModule2(
        game.renderer,
        game.input,
        game.world,
        layerModule,
        sceneTabView
    )

    fun getModules() = setOf(
        referenceActorModule,
        sceneModule,
        cameraModule,
        layerModule
    )

    fun init() {
        cameraModule.init()
        layerModule.init()
    }
}
