package bke.iso.editor.scene

import bke.iso.editor.CheckableContextMenuSelection
import bke.iso.editor.DefaultContextMenuSelection
import bke.iso.editor.EditorEvent
import bke.iso.editor.OpenContextMenuEvent
import bke.iso.editor.scene.camera.CameraModule
import bke.iso.editor.scene.camera.ToggleHideWallsEvent
import bke.iso.editor.scene.layer.LayerModule
import bke.iso.editor.scene.layer.ToggleHighlightLayerEvent
import bke.iso.editor.scene.layer.ToggleUpperLayersHiddenEvent
import bke.iso.editor.scene.tool.ToolModule
import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import com.badlogic.gdx.math.Vector2
import io.github.oshai.kotlinlogging.KotlinLogging

data class OpenViewMenuEvent(val pos: Vector2) : EditorEvent()

data class OpenBuildingsMenuEvent(val pos: Vector2) : EditorEvent()

class SceneTabViewController(
    private val engine: Engine,
    private val sceneTabView: SceneTabView
) {

    private val log = KotlinLogging.logger { }

    private val referenceActorModule = ReferenceActorModule(engine.world)

    private val sceneModule = SceneModule(
        engine.dialogs,
        engine.serializer,
        engine.world,
        engine.assets,
        referenceActorModule,
        engine.events
    )

    val layerModule = LayerModule(
        sceneTabView,
        engine.world,
        engine.events,
        engine.renderer
    )

    private val cameraModule = CameraModule(
        engine.renderer,
        engine.input,
        engine.world,
        layerModule,
        sceneTabView
    )

    val toolModule = ToolModule(
        engine.collisions,
        engine.world,
        referenceActorModule,
        engine.renderer,
        layerModule,
        sceneTabView,
        engine.events
    )

    private val sceneInspectorModule = SceneInspectorModule(
        sceneTabView.sceneInspectorView,
        engine.world,
        engine.events
    )

    private val buildingsModule = BuildingsModule(
        engine.world,
        engine.renderer,
        sceneTabView,
        layerModule,
        engine.assets,
        engine.events
    )

    fun getModules(): Set<Module> = setOf(
        referenceActorModule,
        sceneModule,
        cameraModule,
        layerModule,
        toolModule,
        sceneInspectorModule,
        buildingsModule
    )

    fun init() {
        cameraModule.init()
        layerModule.init()
    }

    fun draw() {
        cameraModule.draw()
        toolModule.draw()
        buildingsModule.draw()
    }

    fun handleEvent(event: EditorEvent) {
        when (event) {
            is OpenViewMenuEvent -> openViewMenu(event.pos)
            is OpenBuildingsMenuEvent -> openBuildingsMenu(event.pos)
        }
    }

    fun openContextMenu(pos: Vector2) {
        val event = OpenContextMenuEvent(
            pos,
            setOf(
                DefaultContextMenuSelection("Edit Building") {
                    sceneTabView.openEditBuildingDialog(engine.world.buildings.getAll()) { name ->
                        log.info { "Selected building '$name'" }
                        buildingsModule.selectBuilding(name)
                    }
                },

                DefaultContextMenuSelection("Close Building") {
                    buildingsModule.closeBuilding()
                },
            )
        )

        engine.events.fire(event)
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
                        engine.events.fire(ToggleHideWallsEvent())
                    }
                ),

                CheckableContextMenuSelection(
                    text = "Hide Upper Layers",
                    isChecked = { layerModule.hideUpperLayers },
                    action = {
                        engine.events.fire(ToggleUpperLayersHiddenEvent())
                    }
                ),

                CheckableContextMenuSelection(
                    text = "Highlight Selected Layer",
                    isChecked = { layerModule.highlightLayer },
                    action = {
                        engine.events.fire(ToggleHighlightLayerEvent())
                    }
                )
            )
        )

        engine.events.fire(event)
    }

    private fun openBuildingsMenu(pos: Vector2) {
        val event = OpenContextMenuEvent(
            pos,
            setOf(
                DefaultContextMenuSelection("New Building") {
                    sceneTabView.openNewBuildingDialog { name ->
                        log.info { "Created new building '$name'" }
                        buildingsModule.selectBuilding(name)
                    }
                },

                DefaultContextMenuSelection("Edit Building") {
                    sceneTabView.openEditBuildingDialog(engine.world.buildings.getAll()) { name ->
                        log.info { "Selected building '$name'" }
                        buildingsModule.selectBuilding(name)
                    }
                },

                DefaultContextMenuSelection("Close Building") {
                    buildingsModule.closeBuilding()
                },

                DefaultContextMenuSelection("Delete Building") {}
            )
        )

        engine.events.fire(event)
    }
}
