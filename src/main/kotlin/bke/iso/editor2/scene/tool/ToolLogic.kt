package bke.iso.editor2.scene.tool

import bke.iso.editor2.scene.SceneMode
import bke.iso.editor2.scene.WorldLogic
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
        tool.update(sceneMode.selectedLayer, renderer.pointer.pos)
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
            ToolSelection.NONE -> null
        }
}
