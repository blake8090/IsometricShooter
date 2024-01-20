package bke.iso.editor

import bke.iso.editor.camera.CameraModule
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.tool.EditorCommand
import bke.iso.editor.tool.ToolModule
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class MainViewPressEvent : EditorEvent()

class MainViewDragEvent : EditorEvent()

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)

    private val referenceActors = ReferenceActors(game.world)
    private val layerModule = LayerModule(
        editorScreen,
        game.world,
        game.events
    )
    private val toolModule = ToolModule(
        game.collisions,
        game.world,
        referenceActors,
        game.renderer,
        layerModule,
        editorScreen
    )
    private val cameraModule = CameraModule(
        game.renderer,
        game.input,
        editorScreen,
        game.world,
        layerModule
    )
    private val sceneModule = SceneModule(
        game.dialogs,
        game.serializer,
        game.world,
        game.assets,
        referenceActors
    )
    private val buildingsModule = BuildingsModule(
        game.world,
        game.renderer,
        editorScreen,
        game.assets
    )
    private val contextMenuModule = ContextMenuModule(editorScreen, buildingsModule)
    override val modules = setOf(layerModule, toolModule, cameraModule, sceneModule, contextMenuModule, buildingsModule)

    override val systems = linkedSetOf<System>()

    private var gridWidth = 20
    private var gridLength = 20
    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }

        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)

        cameraModule.init()
        layerModule.init()
        buildingsModule.init()
    }

    fun handleEvent(event: EditorEvent) {
        when (event) {
            is MainViewPressEvent -> {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    toolModule.performAction()?.let(::execute)
                    editorScreen.closeContextMenu()
                }
            }

            is MainViewDragEvent -> {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    toolModule.performMultiAction()?.let(::execute)
                }
            }

            else -> {
                game.events.fire(event)
            }
        }
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        drawGrid()
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
                Color.WHITE
            )
        }

        for (y in 0..gridLength) {
            game.renderer.bgShapes.addLine(
                Vector3(0f, y.toFloat(), layerModule.selectedLayer.toFloat()),
                Vector3(gridWidth.toFloat(), y.toFloat(), layerModule.selectedLayer.toFloat()),
                0.5f,
                Color.WHITE
            )
        }
    }
}
