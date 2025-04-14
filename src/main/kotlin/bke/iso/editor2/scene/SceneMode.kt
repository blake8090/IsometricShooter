package bke.iso.editor2.scene

import bke.iso.editor2.EditorMode
import bke.iso.editor2.scene.tool.ToolLogic
import bke.iso.editor2.scene.tool.ToolSelection
import bke.iso.engine.Engine
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.core.Event
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneMode(engine: Engine) : EditorMode(engine.renderer, engine.world) {

    private val log = KotlinLogging.logger { }

    var selectedLayer = 0f
        private set

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private val cameraLogic = CameraLogic(engine.input, world, renderer, this)
    private val worldLogic = WorldLogic(world, engine.assets, engine.events, engine.dialogs, engine.serializer)
    private val toolLogic =
        ToolLogic(this, engine.input, engine.collisions, engine.renderer, engine.events, world, worldLogic)
    private val view = SceneModeView(engine.assets, engine.events)

    override fun start() {
        cameraLogic.start()
        toolLogic.start()
    }

    override fun stop() {
        cameraLogic.stop()
    }

    override fun update() {
        cameraLogic.update()
        toolLogic.update()
        drawGrid()
    }

    private fun drawGrid() {
        val shapes = getShapesArray()

        for (x in 0..gridWidth) {
            shapes.addLine(
                Vector3(x.toFloat(), 0f, selectedLayer),
                Vector3(x.toFloat(), gridLength.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
        for (y in 0..gridLength) {
            shapes.addLine(
                Vector3(0f, y.toFloat(), selectedLayer),
                Vector3(gridWidth.toFloat(), y.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
    }

    private fun getShapesArray() =
        if (drawGridForeground) {
            renderer.fgShapes
        } else {
            renderer.bgShapes
        }

    override fun draw() {
        view.draw(toolLogic.selection)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is OpenSceneClicked -> openScene()
            is SaveSceneClicked -> saveScene()
            is ToolSelected -> toolLogic.selectTool(event.selection)
            is TilePrefabSelected -> toolLogic.onTilePrefabSelected(event.prefab)
            is ActorPrefabSelected -> toolLogic.onActorPrefabSelected(event.prefab)
        }
    }

    private fun openScene() {
        worldLogic.loadScene()
    }

    private fun saveScene() {
    }

    class OpenSceneClicked : Event

    class SaveSceneClicked : Event

    data class ToolSelected(val selection: ToolSelection) : Event

    data class TilePrefabSelected(val prefab: TilePrefab) : Event

    data class ActorPrefabSelected(val prefab: ActorPrefab) : Event
}
