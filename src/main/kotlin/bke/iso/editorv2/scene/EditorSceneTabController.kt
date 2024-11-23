package bke.iso.editorv2.scene

import bke.iso.editorv2.scene.camera.CameraModule2
import bke.iso.editorv2.ui.EditorScreen2
import bke.iso.engine.Game

class EditorSceneTabController(
    game: Game,
    editorScreen: EditorScreen2
) {

    private val referenceActorModule = ReferenceActorModule(game.world)

    private val sceneModule = SceneModule(
        game.dialogs,
        game.serializer,
        game.world,
        game.assets,
        referenceActorModule
    )

    private val cameraModule = CameraModule2(
        game.renderer,
        game.input,
        game.world,
        editorScreen.editorSceneTab
    )

    fun getModules() = setOf(
        referenceActorModule,
        sceneModule,
        cameraModule
    )

    fun init() {
        cameraModule.init()
    }
}
