package bke.iso.editor

import bke.iso.editor.scene.SceneTabViewController
import bke.iso.editor.ui.EditorScreen
import bke.iso.editor.ui.Tab
import bke.iso.editor.ui.color
import bke.iso.engine.Game
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.input.ButtonState
import bke.iso.engine.state.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
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
    private val sceneTabController = SceneTabViewController(game, editorScreen.sceneTabView)

    private val contextMenuModule = ContextMenuModule(editorScreen)

    override val modules: Set<Module> =
        sceneTabController.getModules() + contextMenuModule

    override val systems: LinkedHashSet<System> =
        linkedSetOf()

    private var gridWidth = 20
    private var gridLength = 20
    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }
        game.ui.setScreen(editorScreen)

        sceneTabController.init()

        with(game.input.keyMouse) {
            bindMouse("toolDown", Input.Buttons.LEFT, ButtonState.DOWN)
            bindMouse("toolPress", Input.Buttons.LEFT, ButtonState.PRESSED)
            bindMouse("toolRelease", Input.Buttons.LEFT, ButtonState.RELEASED)
            bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)

            bindKey("resetZoom", Input.Keys.Q, ButtonState.PRESSED)
            bindKey("moveCamera", Input.Keys.C, ButtonState.PRESSED)
        }
    }

    fun handleEvent(event: EditorEvent) {
        log.debug { "Fired event ${event::class.simpleName}" }

        if (event is MainViewDragEvent) {
            game.input.onAction("toolDown") {
                sceneTabController.toolModule.performMultiAction()?.let(::execute)
            }
        } else {
            game.events.fire(event)
            sceneTabController.handleEvent(event)
        }
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (editorScreen.sceneTabView.hitTouchableArea()) {
            game.input.onAction("toolPress") {
                println("tool press")
                sceneTabController.toolModule.performAction()?.let(::execute)
            }

            game.input.onAction("toolRelease") {
                println("tool release")
                sceneTabController.toolModule.performReleaseAction()?.let(::execute)
            }
        }

        drawGrid()
        drawTaggedActors()
        sceneTabController.draw()

        game.input.onAction("openContextMenu") {
            openContextMenu()
        }
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
                Vector3(x.toFloat(), 0f, sceneTabController.layerModule.selectedLayer.toFloat()),
                Vector3(x.toFloat(), gridLength.toFloat(), sceneTabController.layerModule.selectedLayer.toFloat()),
                0.5f,
                Color.WHITE
            )
        }

        for (y in 0..gridLength) {
            game.renderer.bgShapes.addLine(
                Vector3(0f, y.toFloat(), sceneTabController.layerModule.selectedLayer.toFloat()),
                Vector3(gridWidth.toFloat(), y.toFloat(), sceneTabController.layerModule.selectedLayer.toFloat()),
                0.5f,
                Color.WHITE
            )
        }
    }

    private fun openContextMenu() {
        when (editorScreen.activeTab) {
            Tab.SCENE -> {
                log.debug { "Delegating context menu to SCENE tab" }
                sceneTabController.openContextMenu(game.renderer.pointer.screenPos)
            }

            Tab.ACTOR -> {
                log.debug { "Delegating context menu to ACTOR tab" }
            }

            else -> {
                log.warn { "No active tab selected - cannot open context menu" }
            }
        }
    }

    private fun drawTaggedActors() {
        game.world.actors.each<Tags> { actor, tags ->
            val box = actor.getCollisionBox()
            if (box != null && tags.tags.isNotEmpty()) {
                game.renderer.fgShapes.addBox(box, 1f, color(46, 125, 50))
            }
        }
    }
}
