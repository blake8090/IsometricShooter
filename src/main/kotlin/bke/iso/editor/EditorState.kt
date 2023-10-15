package bke.iso.editor

import bke.iso.editor.brush.BrushTool
import bke.iso.editor.eraser.EraserTool
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.event.OpenSceneEvent
import bke.iso.editor.event.SaveSceneEvent
import bke.iso.editor.event.SelectActorPrefabEvent
import bke.iso.editor.event.SelectBrushToolEvent
import bke.iso.editor.event.SelectEraserToolEvent
import bke.iso.editor.event.SelectPointerToolEvent
import bke.iso.editor.event.SelectTilePrefabEvent
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.scene.ActorRecord
import bke.iso.engine.Game
import bke.iso.engine.scene.Scene
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.scene.TileRecord
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.world.Actor
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.encodeToString
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

            is OpenSceneEvent -> {
                loadScene()
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

        val ctrlPressed =
            Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
        if (ctrlPressed && Gdx.input.isKeyPressed(Input.Keys.O)) {
            loadScene()
        } else if (ctrlPressed && Gdx.input.isKeyPressed(Input.Keys.S)) {
            saveScene()
        }
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

    private fun loadScene() {
        val file = game.dialogs.showOpenFileDialog() ?: return
        val scene = game.serializer.read<Scene>(file.readText())

        game.world.clear()

        for (record in scene.actors) {
            val prefab = game.assets.get<ActorPrefab>(record.prefab)
            createReferenceActor(game.world, record.pos, prefab)
        }

        for (record in scene.tiles) {
            val prefab = game.assets.get<TilePrefab>(record.prefab)
            createReferenceActor(game.world, record.location, prefab)
        }

        log.info { "Loaded scene: '${file.canonicalPath}'" }
    }

    private fun saveScene() {
        val file = game.dialogs.showSaveFileDialog() ?: return

        val actors = mutableListOf<ActorRecord>()
        game.world.actors.each { actor: Actor, reference: ActorPrefabReference ->
            actors.add(ActorRecord(actor.pos, reference.prefab))
        }

        val tiles = mutableListOf<TileRecord>()
        game.world.actors.each { actor: Actor, reference: TilePrefabReference ->
            tiles.add(TileRecord(Location(actor.pos), reference.prefab))
        }

        val scene = Scene("1", actors, tiles)
        val content = game.serializer.format.encodeToString(scene)
        file.writeText(content)
        log.info { "Saved scene: '${file.canonicalPath}'" }
    }
}
