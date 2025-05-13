package bke.iso.editor.scene.tool

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.SceneMode
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.prefab.EntityPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.Events
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import io.github.oshai.kotlinlogging.KotlinLogging
import com.badlogic.gdx.Input as GdxInput

enum class ToolSelection {
    BRUSH,
    POINTER,
    ERASER,
    FILL,
    ROOM,
    NONE
}

class ToolLogic(
    private val sceneMode: SceneMode,
    private val input: Input,
    collisions: Collisions,
    private val renderer: Renderer,
    events: Events,
    world: World,
    worldLogic: WorldLogic
) {

    private val log = KotlinLogging.logger { }

    private val pointerTool = PointerTool(collisions, renderer, events)
    private val brushTool = BrushTool(collisions, world, worldLogic, renderer)
    private val eraserTool = EraserTool(collisions, worldLogic, renderer)
    private val fillTool = FillTool(collisions, renderer, worldLogic)
    private val roomTool = RoomTool(collisions, renderer, worldLogic)

    var selection = ToolSelection.NONE
        private set
    private var currentTool: BaseTool? = null

    fun start() {
        input.keyMouse.bindMouse("sceneTabToolDown", GdxInput.Buttons.LEFT, ButtonState.DOWN)
        input.keyMouse.bindMouse("sceneTabToolPress", GdxInput.Buttons.LEFT, ButtonState.PRESSED)
        input.keyMouse.bindMouse("sceneTabToolRelease", GdxInput.Buttons.LEFT, ButtonState.RELEASED)
    }

    fun update() {
        val tool = currentTool ?: return
        // TODO: scale cursor position when screen size changes
        tool.update(sceneMode.selectedLayer.toFloat(), renderer.pointer.pos)
        tool.draw()
    }

    fun selectTool(newSelection: ToolSelection) {
        currentTool?.disable()

        val newTool = getToolInstance(newSelection)
        currentTool = newTool
        selection = newSelection

        if (newTool != null) {
            newTool.enable()
            log.debug { "Selected tool: ${newTool::class.simpleName}" }
        }
    }

    private fun getToolInstance(selection: ToolSelection) =
        when (selection) {
            ToolSelection.BRUSH -> brushTool
            ToolSelection.POINTER -> pointerTool
            ToolSelection.ERASER -> eraserTool
            ToolSelection.FILL -> fillTool
            ToolSelection.ROOM -> roomTool
            ToolSelection.NONE -> null
        }

    fun onTilePrefabSelected(prefab: TilePrefab) {
        brushTool.selectPrefab(prefab)
        fillTool.selectPrefab(prefab)
    }

    fun onEntityPrefabSelected(prefab: EntityPrefab) {
        brushTool.selectPrefab(prefab)
        fillTool.selectPrefab(prefab)
        roomTool.selectedPrefab = prefab
    }

    fun performAction(): EditorCommand? =
        currentTool?.performAction()

    fun performMultiAction(): EditorCommand? =
        currentTool?.performMultiAction()

    fun performReleaseAction(): EditorCommand? =
        currentTool?.performReleaseAction()
}
