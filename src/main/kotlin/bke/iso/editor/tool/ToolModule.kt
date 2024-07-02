package bke.iso.editor.tool

import bke.iso.editor.ReferenceActors
import bke.iso.editor.tool.brush.BrushTool
import bke.iso.editor.tool.eraser.EraserTool
import bke.iso.editor.ui.EditorScreen
import bke.iso.editor.LayerModule
import bke.iso.editor.tool.fill.FillTool
import bke.iso.editor.tool.room.RoomTool
import bke.iso.engine.Event
import bke.iso.engine.state.Module
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import mu.KotlinLogging

class ToolModule(
    collisions: Collisions,
    world: World,
    referenceActors: ReferenceActors,
    private val renderer: Renderer,
    private val layerModule: LayerModule,
    private val editorScreen: EditorScreen
) : Module {

    private val log = KotlinLogging.logger {}

    private val pointerTool = PointerTool(collisions, renderer)
    private val brushTool = BrushTool(collisions, world, referenceActors, renderer)
    private val eraserTool = EraserTool(collisions, referenceActors, renderer)
    private val roomTool = RoomTool(collisions, renderer, referenceActors)
    private val fillTool = FillTool(collisions, renderer, referenceActors)

    private var selectedTool: EditorTool? = null

    override fun update(deltaTime: Float) {
        val tool = selectedTool ?: return
        // TODO: scale cursor position when screen size changes
        tool.update(layerModule.selectedLayer.toFloat(), renderer.pointer.pos)
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

    fun draw() {
        selectedTool?.draw()
    }

    fun performAction(): EditorCommand? =
        selectedTool?.performAction()

    fun performMultiAction(): EditorCommand? =
        selectedTool?.performMultiAction()

    fun performReleaseAction(): EditorCommand? =
        selectedTool?.performReleaseAction()

    private fun selectTool(tool: EditorTool) {
        selectedTool?.disable()
        if (selectedTool is BrushTool) {
            editorScreen.unselectPrefabs()
        }

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
