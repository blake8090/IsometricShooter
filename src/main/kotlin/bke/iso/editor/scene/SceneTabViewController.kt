package bke.iso.editor.scene

import bke.iso.editor.CheckableContextMenuSelection
import bke.iso.editor.DefaultContextMenuSelection
import bke.iso.editor.EditorEvent
import bke.iso.editor.OpenContextMenuEvent
import bke.iso.editor.PerformCommandEvent
import bke.iso.editor.scene.camera.CameraModule
import bke.iso.editor.scene.camera.ToggleHideWallsEvent
import bke.iso.editor.scene.layer.LayerModule
import bke.iso.editor.scene.layer.ToggleHighlightLayerEvent
import bke.iso.editor.scene.layer.ToggleUpperLayersHiddenEvent
import bke.iso.editor.scene.tool.ToolModule
import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.editor.ui.color
import bke.iso.engine.Engine
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Module
import bke.iso.engine.input.ButtonState
import bke.iso.engine.world.actor.Tags
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class MainViewPressEvent : EditorEvent()

class MainViewDragEvent : EditorEvent()

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

    private val layerModule = LayerModule(
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

    private val toolModule = ToolModule(
        engine.collisions,
        engine.world,
        referenceActorModule,
        engine.renderer,
        layerModule,
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

    private var gridWidth = 20
    private var gridLength = 20

    private var drawGridForeground = false

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
        with(engine.input.keyMouse) {
            bindMouse("sceneTabToolDown", Input.Buttons.LEFT, ButtonState.DOWN)
            bindMouse("sceneTabToolPress", Input.Buttons.LEFT, ButtonState.PRESSED)
            bindMouse("sceneTabToolRelease", Input.Buttons.LEFT, ButtonState.RELEASED)
        }
    }

    fun update() {
        if (sceneTabView.hitTouchableArea()) {
            engine.input.onAction("sceneTabToolPress") {
                toolModule.performAction()?.let { command ->
                    engine.events.fire(PerformCommandEvent(command))
                }
            }

            engine.input.onAction("sceneTabToolRelease") {
                toolModule.performReleaseAction()?.let { command ->
                    engine.events.fire(PerformCommandEvent(command))
                }
            }
        }

        drawGrid()
        drawTaggedActors()
    }

    fun handleEditorEvent(event: EditorEvent) {
        when (event) {
            is MainViewDragEvent -> performMultiAction()
            is OpenViewMenuEvent -> openViewMenu(event.pos)
            is OpenBuildingsMenuEvent -> openBuildingsMenu(event.pos)
        }
    }

    private fun performMultiAction() {
        engine.input.onAction("sceneTabToolDown") {
            toolModule.performMultiAction()?.let { command ->
                engine.events.fire(PerformCommandEvent(command))
            }
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
                ),

                CheckableContextMenuSelection(
                    text = "Show Grid in Foreground",
                    isChecked = { drawGridForeground },
                    action = {
                        drawGridForeground = !drawGridForeground
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

    private fun drawGrid() {
        if (drawGridForeground) {
            for (x in 0..gridWidth) {
                engine.renderer.fgShapes.addLine(
                    Vector3(x.toFloat(), 0f, layerModule.selectedLayer.toFloat()),
                    Vector3(x.toFloat(), gridLength.toFloat(), layerModule.selectedLayer.toFloat()),
                    0.5f,
                    Color.WHITE
                )
            }

            for (y in 0..gridLength) {
                engine.renderer.fgShapes.addLine(
                    Vector3(0f, y.toFloat(), layerModule.selectedLayer.toFloat()),
                    Vector3(gridWidth.toFloat(), y.toFloat(), layerModule.selectedLayer.toFloat()),
                    0.5f,
                    Color.WHITE
                )
            }
        } else {
            for (x in 0..gridWidth) {
                engine.renderer.bgShapes.addLine(
                    Vector3(x.toFloat(), 0f, layerModule.selectedLayer.toFloat()),
                    Vector3(x.toFloat(), gridLength.toFloat(), layerModule.selectedLayer.toFloat()),
                    0.5f,
                    Color.WHITE
                )
            }

            for (y in 0..gridLength) {
                engine.renderer.bgShapes.addLine(
                    Vector3(0f, y.toFloat(), layerModule.selectedLayer.toFloat()),
                    Vector3(gridWidth.toFloat(), y.toFloat(), layerModule.selectedLayer.toFloat()),
                    0.5f,
                    Color.WHITE
                )
            }
        }
    }

    private fun drawTaggedActors() {
        engine.world.actors.each<Tags> { actor, tags ->
            val box = actor.getCollisionBox()
            if (box != null && tags.tags.isNotEmpty()) {
                engine.renderer.fgShapes.addBox(box, 1f, color(46, 125, 50))
            }
        }
    }
}
