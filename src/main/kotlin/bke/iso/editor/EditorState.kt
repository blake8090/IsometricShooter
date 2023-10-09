package bke.iso.editor

import bke.iso.editor.brush.BrushTool
import bke.iso.editor.eraser.EraserTool
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.event.SaveSceneEvent
import bke.iso.editor.event.SelectActorPrefabEvent
import bke.iso.editor.event.SelectBrushToolEvent
import bke.iso.editor.event.SelectEraserToolEvent
import bke.iso.editor.event.SelectPointerToolEvent
import bke.iso.editor.event.SelectTilePrefabEvent
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.ActorRecord
import bke.iso.engine.Game
import bke.iso.engine.Scene
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.asset.BASE_PATH
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.util.nfd.NFDFilterItem
import org.lwjgl.util.nfd.NativeFileDialog.NFD_CANCEL
import org.lwjgl.util.nfd.NativeFileDialog.NFD_ERROR
import org.lwjgl.util.nfd.NativeFileDialog.NFD_FreePath
import org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError
import org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY
import org.lwjgl.util.nfd.NativeFileDialog.NFD_SaveDialog
import kotlin.io.path.Path

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

data class ActorPrefabReference(val prefab: String) : Component

class EditorState(override val game: Game) : State() {

    override val systems = emptySet<System>()

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.RIGHT)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private val pointerTool = PointerTool(game.collisions, game.renderer)
    private val brushTool = BrushTool(game.world, game.renderer)
    private val eraserTool = EraserTool(game.world, game.renderer, game.collisions)
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
                brushTool.selectPrefab(event.prefab)
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

            is SaveSceneEvent -> {
                saveScene()
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

    private fun saveScene() {
        val path = openSaveDialog() ?: return

        val actors = mutableListOf<ActorRecord>()
        game.world.actors.each { actor: Actor, ref: ActorPrefabReference ->
            actors.add(ActorRecord(actor.pos, ref.prefab))
        }
        log.debug { "saving ${actors.size} actor records" }

        val scene = Scene("1", actors, emptyList())
        val content = game.serializer.format.encodeToString(scene)
        game.files.writeFile(path, content)
        log.info { "Saved scene to '$path'" }
    }

    private fun openSaveDialog(): String? {
        log.info { "Opening save scene dialog" }

        stackPush().use { stack ->
            val filters = NFDFilterItem.malloc(1)
            filters[0]
                .name(stack.UTF8("Scene"))
                .spec(stack.UTF8("scene"))

            val pathPointer = stack.mallocPointer(1)
            // TODO: investigate why default path setting does not work
            val defaultPath = Path(BASE_PATH)
                .toAbsolutePath()
                .toString()
            val result = NFD_SaveDialog(pathPointer, filters, defaultPath, "untitled.scene")

            when (result) {
                NFD_OKAY -> {
                    val path = pathPointer.getStringUTF8(0)
                    NFD_FreePath(pathPointer.get(0))
                    return path
                }

                NFD_CANCEL -> {
                    log.debug { "User cancelled save" }
                }

                NFD_ERROR -> {
                    log.error { "Error saving scene: ${NFD_GetError()}" }
                }
            }
        }

        return null
    }
}
