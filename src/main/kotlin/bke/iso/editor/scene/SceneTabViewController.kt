package bke.iso.editor.scene

import bke.iso.editor.CheckableContextMenuSelection
import bke.iso.editor.EditorEvent
import bke.iso.editor.OpenContextMenuEvent
import bke.iso.editor.scene.camera.CameraModule
import bke.iso.editor.scene.camera.ToggleHideWallsEvent
import bke.iso.editor.scene.layer.LayerModule
import bke.iso.editor.scene.layer.ToggleHighlightLayerEvent
import bke.iso.editor.scene.layer.ToggleUpperLayersHiddenEvent
import bke.iso.editor.scene.tool.ToolModule
import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.engine.Game
import com.badlogic.gdx.math.Vector2

data class OpenViewMenuEvent(val pos: Vector2) : EditorEvent()

data class OpenBuildingsMenuEvent(val pos: Vector2) : EditorEvent()

class SceneTabViewController(
    private val game: Game,
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
        sceneTabView,
        game.events
    )

    private val sceneInspectorModule = SceneInspectorModule(
        sceneTabView.sceneInspectorView
    )

    fun getModules() = setOf(
        referenceActorModule,
        sceneModule,
        cameraModule,
        layerModule,
        toolModule,
        sceneInspectorModule
    )

    fun init() {
        cameraModule.init()
        layerModule.init()
    }

    fun draw() {
        cameraModule.draw()
        toolModule.draw()
    }

    fun handleEvent(event: EditorEvent) {
        if (event is OpenViewMenuEvent) {
            openViewMenu(event.pos)
        }
    }

    private fun openViewMenu(pos: Vector2) {
        val event = OpenContextMenuEvent(
            pos,
            setOf(
                CheckableContextMenuSelection(
                    text = "Show Grid",
                    isChecked = { false },
                    action = {}
                ),

                CheckableContextMenuSelection(
                    text = "Show Collision",
                    isChecked = { false },
                    action = {}
                ),

                CheckableContextMenuSelection(
                    text = "Hide Walls",
                    isChecked = { cameraModule.wallsHidden },
                    action = {
                        game.events.fire(ToggleHideWallsEvent())
                    }
                ),

                CheckableContextMenuSelection(
                    text = "Hide Upper Layers",
                    isChecked = { layerModule.hideUpperLayers },
                    action = {
                        game.events.fire(ToggleUpperLayersHiddenEvent())
                    }
                ),

                CheckableContextMenuSelection(
                    text = "Highlight Selected Layer",
                    isChecked = { layerModule.highlightLayer },
                    action = {
                        game.events.fire(ToggleHighlightLayerEvent())
                    }
                ),
            )
        )

        game.events.fire(event)
    }
}
