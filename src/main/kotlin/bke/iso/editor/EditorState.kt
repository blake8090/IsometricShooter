package bke.iso.editor

import bke.iso.editor.camera.CameraModule
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.layer.LayerModule
import bke.iso.editor.tool.EditorCommand
import bke.iso.editor.tool.ToolModule
import bke.iso.editor.ui.EditorScreen
import bke.iso.editor.ui.color
import bke.iso.engine.Game
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.world.actor.Tags
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class MainViewPressEvent : EditorEvent()

class MainViewDragEvent : EditorEvent()

class PerformActionEvent : EditorEvent()

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)

    private val referenceActors = ReferenceActors(game.world)
    private val layerModule = LayerModule(
        editorScreen,
        game.world,
        game.events,
        game.renderer
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
        layerModule,
        game.assets
    )
    private val contextMenuModule = ContextMenuModule(
        editorScreen,
        buildingsModule,
        game.world,
        toolModule
    )
    override val modules = setOf(
        layerModule,
        buildingsModule,
        toolModule,
        cameraModule,
        sceneModule,
        contextMenuModule
    )

    override val systems = linkedSetOf<System>()

    private var gridWidth = 20
    private var gridLength = 20
    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }

        game.ui.setScreen(editorScreen)

        cameraModule.init()
        layerModule.init()
        buildingsModule.init()

        game.input.keyMouse.bind(
            "toolDown" to MouseBinding(Input.Buttons.LEFT, ButtonState.DOWN),
            "toolPress" to MouseBinding(Input.Buttons.LEFT, ButtonState.PRESSED),
            "toolRelease" to MouseBinding(Input.Buttons.LEFT, ButtonState.RELEASED)
        )
    }

    fun handleEvent(event: EditorEvent) {
        when (event) {
            is MainViewDragEvent -> {
                game.input.onAction("toolDown") {
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

        if (editorScreen.hitMainView()) {
            game.input.onAction("toolPress") {
                println("tool press")
                toolModule.performAction()?.let(::execute)
                editorScreen.closeContextMenu()
            }

            game.input.onAction("toolRelease") {
                println("tool release")
                toolModule.performReleaseAction()?.let(::execute)
            }
        }

        drawGrid()

        buildingsModule.draw()
        cameraModule.draw()
        drawTaggedActors()
        toolModule.draw()
    }

    private fun execute(command: EditorCommand) {
        log.debug { "Executing ${command::class.simpleName}" }
        command.execute()
        commands.addFirst(command)
        handleEvent(PerformActionEvent())
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

    private fun drawTaggedActors() {
        game.world.actors.each<Tags> { actor, tags ->
            val box = actor.getCollisionBox() ?: return@each

            if (tags.tags.isNotEmpty()) {
                game.renderer.fgShapes.addBox(box, 1f, color(46, 125, 50))
            }
        }
    }
}
