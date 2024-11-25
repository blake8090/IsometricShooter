package bke.iso.editor.scene

import bke.iso.editor.scene.camera.CameraModule
import bke.iso.editor.scene.layer.LayerModule
import bke.iso.editor.scene.tool.ToolModule
import bke.iso.editor.scene.ui.SceneTabView
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

    val layerModule = LayerModule(
        sceneTabView,
        game.world,
        game.events,
        game.renderer
    )

    private val cameraModule = CameraModule(
        game.renderer,
        game.input,
        game.world,
        layerModule,
        sceneTabView
    )

    val toolModule = ToolModule(
        game.collisions,
        game.world,
        referenceActorModule,
        game.renderer,
        layerModule,
        sceneTabView
    )

    fun getModules() = setOf(
        referenceActorModule,
        sceneModule,
        cameraModule,
        layerModule,
        toolModule
    )

    fun init() {
        cameraModule.init()
        layerModule.init()
    }

    fun draw() {
        cameraModule.draw()
        toolModule.draw()
    }
}
