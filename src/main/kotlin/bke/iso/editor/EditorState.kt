package bke.iso.editor

import bke.iso.editor.camera.CameraModule
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.event.EditorEventWrapper
import bke.iso.editor.event.OpenSceneEvent
import bke.iso.editor.event.SaveSceneEvent
import bke.iso.editor.tool.EditorCommand
import bke.iso.editor.tool.ToolModule
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
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.encodeToString
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val referenceActors = ReferenceActors(game.world)
    private val layerModule = LayerModule(editorScreen)
    private val toolModule = ToolModule(
        game.collisions,
        game.world,
        referenceActors,
        game.renderer,
        layerModule,
        editorScreen
    )
    private val cameraModule = CameraModule(game.renderer, game.input, editorScreen)
    override val modules = setOf(layerModule, toolModule, cameraModule)
    override val systems = emptySet<System>()

    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }

        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)

        cameraModule.init()
        layerModule.init()
    }

    fun handleEvent(event: EditorEvent) =
        when (event) {
            is SaveSceneEvent -> {
                saveScene()
            }

            is OpenSceneEvent -> {
                loadScene()
            }

            else -> {
                game.events.fire(EditorEventWrapper(event))
            }
        }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        updateTool()
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
        if (editorScreen.hitMainView()) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                toolModule.performAction()?.let(::execute)
            } else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                toolModule.performMultiAction()?.let(::execute)
            }
        }
    }

    private fun execute(command: EditorCommand) {
        log.debug { "Executing ${command::class.simpleName}" }
        command.execute()
        commands.addFirst(command)
    }

    private fun drawGrid() {
        for (x in 0..gridWidth) {
            game.renderer.bgShapes.addLine(
                Vector3(x.toFloat(), 0f, layerModule.selectedLayer.toFloat()),
                Vector3(x.toFloat(), gridLength.toFloat(), layerModule.selectedLayer.toFloat()),
                0.5f,
                Color.GREEN
            )
        }

        for (y in 0..gridLength) {
            game.renderer.bgShapes.addLine(
                Vector3(0f, y.toFloat(), layerModule.selectedLayer.toFloat()),
                Vector3(gridWidth.toFloat(), y.toFloat(), layerModule.selectedLayer.toFloat()),
                0.5f,
                Color.GREEN
            )
        }
    }

    private fun loadScene() {
        val file = game.dialogs.showOpenFileDialog() ?: return
        val scene = game.serializer.read<Scene>(file.readText())

        referenceActors.clear()
        game.world.clear()

        for (record in scene.actors) {
            val prefab = game.assets.get<ActorPrefab>(record.prefab)
            referenceActors.create(prefab, record.pos)
        }

        for (record in scene.tiles) {
            val prefab = game.assets.get<TilePrefab>(record.prefab)
            referenceActors.create(prefab, record.location)
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
