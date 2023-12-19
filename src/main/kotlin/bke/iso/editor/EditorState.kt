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

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)

    private val referenceActors = ReferenceActors(game.world)
    private val layerModule = LayerModule(editorScreen, game.world)
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
        editorScreen
    )
    private val sceneModule = SceneModule(
        game.dialogs,
        game.serializer,
        game.world,
        game.assets,
        referenceActors
    )
    override val modules = setOf(layerModule, toolModule, cameraModule, sceneModule)
    override val systems = emptySet<System>()

    private var gridWidth = 20
    private var gridLength = 20
    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }

        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)

        cameraModule.init()
        layerModule.init()
    }

    fun handleEvent(event: EditorEvent) {
        game.events.fire(event)//EditorEventWrapper(event))
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        updateTool()
        drawGrid()
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
}
