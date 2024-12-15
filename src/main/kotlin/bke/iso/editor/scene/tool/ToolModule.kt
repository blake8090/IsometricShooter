package bke.iso.editor.scene.tool

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.ReferenceActorModule
import bke.iso.editor.scene.layer.LayerModule
import bke.iso.editor.scene.tool.brush.BrushTool
import bke.iso.editor.scene.tool.eraser.EraserTool
import bke.iso.editor.scene.tool.fill.FillTool
import bke.iso.editor.scene.tool.room.RoomTool
import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.Module
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import io.github.oshai.kotlinlogging.KotlinLogging

class ToolModule(
    collisions: Collisions,
    world: World,
    referenceActorModule: ReferenceActorModule,
    private val renderer: Renderer,
    private val layerModule: LayerModule,
    private val sceneTabView: SceneTabView,
    events: Events
) : Module {

    private val log = KotlinLogging.logger {}

    private val pointerTool = PointerTool(collisions, renderer, events)
    private val brushTool = BrushTool(collisions, world, referenceActorModule, renderer)
    private val eraserTool = EraserTool(collisions, referenceActorModule, renderer)
    private val roomTool = RoomTool(collisions, renderer, referenceActorModule)
    private val fillTool = FillTool(collisions, renderer, referenceActorModule)

    private var selectedTool: SceneTabTool? = null

    override fun update(deltaTime: Float) {
        val tool = selectedTool ?: return
        // TODO: scale cursor position when screen size changes
        tool.update(layerModule.selectedLayer.toFloat(), renderer.pointer.pos)

        tool.draw()
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is SelectTilePrefabEvent -> {
                brushTool.selectPrefab(event.prefab)
                roomTool.selectedPrefab = null
                fillTool.selectPrefab(event.prefab)
            }

            is SelectActorPrefabEvent -> {
                brushTool.selectPrefab(event.prefab)
                roomTool.selectedPrefab = event.prefab
                fillTool.selectPrefab(event.prefab)
            }

            is SelectPointerToolEvent -> {
                selectTool(pointerTool)
            }

            is SelectBrushToolEvent -> {
                selectTool(brushTool)
            }

            is SelectEraserToolEvent -> {
                selectTool(eraserTool)
            }

            is SelectRoomToolEvent -> {
                selectTool(roomTool)
            }

            is SelectFillToolEvent -> {
                selectTool(fillTool)
            }
        }
    }

    fun performAction(): EditorCommand? =
        selectedTool?.performAction()

    fun performMultiAction(): EditorCommand? =
        selectedTool?.performMultiAction()

    fun performReleaseAction(): EditorCommand? =
        selectedTool?.performReleaseAction()

    private fun selectTool(tool: SceneTabTool) {
        selectedTool?.disable()
        tool.enable()
        selectedTool = tool
        log.debug { "Selected tool: ${tool::class.simpleName}" }
    }

    fun getSelectedActor(): Actor? {
        if (selectedTool is PointerTool) {
            return (selectedTool as PointerTool).getSelectedActor()
        }
        return null
    }
}
