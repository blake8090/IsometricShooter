package bke.iso.editor.v3.scene.tool

import bke.iso.editor.v3.scene.AssetBrowserView
import bke.iso.editor.v3.scene.SceneTabViewController
import bke.iso.editor.v3.scene.ToolbarView
import bke.iso.editor.v3.scene.world.WorldLogic
import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.Events
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.scenes.scene2d.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import com.badlogic.gdx.Input as GdxInput

class ToolLogic(
    private val sceneTabViewController: SceneTabViewController,
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

    private var selectedTool: BaseTool? = null

    fun start() {
        input.keyMouse.bindMouse("sceneTabToolDown", GdxInput.Buttons.LEFT, ButtonState.DOWN)
        input.keyMouse.bindMouse("sceneTabToolPress", GdxInput.Buttons.LEFT, ButtonState.PRESSED)
        input.keyMouse.bindMouse("sceneTabToolRelease", GdxInput.Buttons.LEFT, ButtonState.RELEASED)
    }

    fun update() {
        val tool = selectedTool ?: return
        // TODO: scale cursor position when screen size changes
        tool.update(sceneTabViewController.selectedLayer, renderer.pointer.pos)
        tool.draw()
    }

    fun handleEvent(event: Event) {
        when (event) {
            is ToolbarView.OnPointerToolSelected -> {
                selectTool(pointerTool)
            }

            is ToolbarView.OnBrushToolSelected -> {
                selectTool(brushTool)
            }

            is AssetBrowserView.OnTilePrefabSelected -> {
                brushTool.selectPrefab(event.prefab)
            }

            is AssetBrowserView.OnActorPrefabSelected -> {
                brushTool.selectPrefab(event.prefab)
            }
        }
    }

    private fun selectTool(tool: BaseTool) {
        selectedTool?.disable()
        tool.enable()
        selectedTool = tool
        log.debug { "Selected tool: ${tool::class.simpleName}" }
    }
}
