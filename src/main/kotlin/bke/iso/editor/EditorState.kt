package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.editor.brush.BrushTool
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

interface EditorCommand {
    fun execute()
    fun undo()
}

interface EditorTool {
    fun update()
    fun performAction(): EditorCommand?
    fun enable()
    fun disable()
}

class EditorState(override val game: Game) : State() {

    override val systems = emptySet<System>()

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.RIGHT)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private val pointerTool = PointerTool()
    private val brushTool = BrushTool(game.world, game.renderer)
    private var selectedTool: EditorTool? = null
    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }

        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)
        game.input.addInputProcessor(mouseDragAdapter)

        brushTool.disable()
    }

    fun handleEvent(event: EditorEvent) =
        when (event) {
            is SelectTilePrefabEvent -> {
                brushTool.selectPrefab(event.prefab)
            }

            is SelectActorPrefabEvent -> {
                brushTool.selectPrefab(event.prefab, event.texture)
            }

            is SelectPointerToolEvent -> {
                selectTool(pointerTool)
            }

            is SelectBrushToolEvent -> {
                selectTool(brushTool)
            }
        }

    private fun selectTool(tool: EditorTool) {
        selectedTool?.disable()
        tool.enable()
        selectedTool = tool
        log.debug { "Selected tool: ${tool::class.simpleName}" }
    }

    override fun update(deltaTime: Float) {
        updateTool()
        panCamera()
        drawGrid()
    }

    private fun updateTool() {
        val tool = selectedTool ?: return
        tool.update()
        if (editorScreen.hitMainView() && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            runToolCommand(tool)
        }
    }

    private fun runToolCommand(tool: EditorTool) {
        val command = tool.performAction() ?: return
        log.debug { "Executing ${command::class.simpleName}" }
        command.execute()
        commands.addFirst(command)
    }

    private fun panCamera() {
        val delta = mouseDragAdapter.getDelta()
        if (delta.isZero) {
            return
        }
        val cameraDelta = Vector2(
            delta.x * cameraPanScale.x * -1, // for some reason the delta's x-axis is inverted!
            delta.y * cameraPanScale.y
        )
        game.renderer.moveCamera(cameraDelta)
    }

    private fun drawGrid() {
        for (x in 0..gridWidth) {
            game.renderer.bgShapes.addLine(
                Vector3(x.toFloat(), 0f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.GREEN
            )
        }

        for (y in 0..gridLength) {
            game.renderer.bgShapes.addLine(
                Vector3(0f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                0.5f,
                Color.GREEN
            )
        }
    }
}
