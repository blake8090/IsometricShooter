package bke.iso.editor.v2

import bke.iso.editor.v2.actor.ActorTabViewController
import bke.iso.editor.v2.core.EditorCommand
import bke.iso.editor.v2.core.EditorEvent
import bke.iso.editor.v2.scene.SceneTabViewController
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorState(override val engine: Engine) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen2 = EditorScreen2(engine.assets)
    private val actorTabViewController =
        ActorTabViewController(
            editorScreen2.skin,
            engine.assets,
            engine.events,
            engine.input,
            engine.dialogs,
            engine.serializer
        )
    private val sceneTabViewController =
        SceneTabViewController(
            editorScreen2.skin,
            engine.assets,
            engine.renderer
        )

    override val modules: Set<Module> = mutableSetOf<Module>().apply {
        add(actorTabViewController)
        addAll(actorTabViewController.modules)
        add(sceneTabViewController)
        addAll(sceneTabViewController.modules)
    }

    override val systems: LinkedHashSet<System> =
        linkedSetOf()

    private val commands = ArrayDeque<EditorCommand>()

    init {
        editorScreen2.actorTabView = actorTabViewController.view
        editorScreen2.sceneTabView = sceneTabViewController.view

        editorScreen2.editorEventListener = EventListener { event ->
            if (event is EditorEvent) {
                handleEditorEvent(event)
            }
            false
        }
    }

    override suspend fun load() {
        log.info { "Loading editor" }
        engine.ui.setScreen(editorScreen2)

//        engine.input.keyMouse.bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        when (event) {
            is ExecuteCommandEvent -> {
                execute(event.command)
            }

            is ShowDialogEvent -> {
                event.dialog.show(editorScreen2.stage)
            }
        }
    }

    private fun onSelectTab(tab: Tab) {
        if (tab == Tab.SCENE) {
            engine.rendererManager.reset()
        } else if (tab == Tab.ACTOR) {
            actorTabViewController.enableRenderer(engine.rendererManager)
        }
    }

    private fun handleEditorEvent(event: EditorEvent) {
        log.debug { "Handling editor event ${event::class.simpleName}" }

        if (event is ExecuteCommandEvent) {
            execute(event.command)
        } else if (event is SelectTabEvent) {
            onSelectTab(event.tab)
        }

        when (editorScreen2.activeTab) {
            Tab.SCENE -> sceneTabViewController.handleEditorEvent(event)

            Tab.ACTOR -> actorTabViewController.handleEditorEvent(event)

            Tab.NONE -> TODO()
        }
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        when (editorScreen2.activeTab) {
            Tab.SCENE -> sceneTabViewController.update(deltaTime)
            Tab.ACTOR -> actorTabViewController.update(deltaTime)
            Tab.NONE -> error("No tab selected")
        }
//
//        engine.input.onAction("openContextMenu") {
//            openContextMenu()
//        }
    }

    private fun execute(command: EditorCommand) {
        log.debug { "Executing command: ${command.name}" }
        command.execute()
        commands.addFirst(command)
    }
//
//    private fun openContextMenu() {
//        when (editorScreen.activeTab) {
//            Tab.SCENE -> {
//                log.debug { "Delegating context menu to SCENE tab" }
//                sceneTabController.openContextMenu(engine.renderer.pointer.screenPos)
//            }
//
//            Tab.ACTOR -> {
//                log.debug { "Delegating context menu to ACTOR tab" }
//            }
//
//            else -> {
//                log.warn { "No active tab selected - cannot open context menu" }
//            }
//        }
//    }

    data class ExecuteCommandEvent(val command: EditorCommand) : EditorEvent()

    class ShowDialogEvent(val dialog: Dialog) : Event

    data class SelectTabEvent(val tab: Tab) : EditorEvent()
}
