package bke.iso.editor

import bke.iso.editor.actor.ActorTabViewController
import bke.iso.editor.scene.SceneTabViewController
import bke.iso.editor.ui.EditorScreen
import bke.iso.editor.ui.Tab
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class PerformActionEvent : Event

data class PerformCommandEvent(val editorCommand: EditorCommand) : EditorEvent()

class EditorState(override val engine: Engine) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, engine.assets)
    private val sceneTabController = SceneTabViewController(engine, editorScreen.sceneTabView)
    private val actorTabViewController = ActorTabViewController(engine, editorScreen.actorTabView)

    private val contextMenuModule = ContextMenuModule(editorScreen)

    override val modules: Set<Module> =
        sceneTabController.getModules() + contextMenuModule + actorTabViewController.getModules()

    override val systems: LinkedHashSet<System> =
        linkedSetOf()

    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }
        engine.ui.setScreen(editorScreen)

        sceneTabController.init()
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        if (event is PerformCommandEvent) {
            println("performing command")
            execute(event.editorCommand)
        }
    }

    fun handleEditorEvent(event: EditorEvent) {
        log.debug { "Fired event ${event::class.simpleName}" }
        sceneTabController.handleEditorEvent(event)
        engine.events.fire(event)
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        when (editorScreen.activeTab) {
            Tab.SCENE -> sceneTabController.update()
            Tab.ACTOR -> actorTabViewController.update()
            Tab.NONE -> TODO()
        }

        engine.input.onAction("openContextMenu") {
            openContextMenu()
        }
    }

    private fun execute(command: EditorCommand) {
        log.debug { "Executing ${command::class.simpleName}" }
        command.execute()
        commands.addFirst(command)
        handleEvent(PerformActionEvent())
    }

    private fun openContextMenu() {
        when (editorScreen.activeTab) {
            Tab.SCENE -> {
                log.debug { "Delegating context menu to SCENE tab" }
                sceneTabController.openContextMenu(engine.renderer.pointer.screenPos)
            }

            Tab.ACTOR -> {
                log.debug { "Delegating context menu to ACTOR tab" }
            }

            else -> {
                log.warn { "No active tab selected - cannot open context menu" }
            }
        }
    }
}
