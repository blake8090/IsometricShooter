package bke.iso.editor.v2

import bke.iso.editor.v2.actor.ActorTabViewController
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

//class PerformActionEvent : Event
//
//data class PerformCommandEvent(val editorCommand: EditorCommand) : EditorEvent()
//
//data class SelectTabEvent(val tab: Tab) : EditorEvent()

class EditorState(override val engine: Engine) : State() {

    private val log = KotlinLogging.logger {}

//    private val editorScreen = EditorScreen(this, engine.assets)
//    private val sceneTabController = SceneTabViewController(engine, editorScreen.sceneTabView)
//    private val actorTabViewController = ActorTabViewController(engine, editorScreen.actorTabView)
//
//    private val contextMenuModule = ContextMenuModule(editorScreen)

//    override val modules: Set<Module> =
//        sceneTabController.getModules() + contextMenuModule + actorTabViewController.getModules()

    private val editorScreen2 = EditorScreen2(engine.assets)
    private val actorTabViewController =
        ActorTabViewController(editorScreen2.skin, engine.assets, engine.events, engine.input)

    override val modules: Set<Module> = mutableSetOf<Module>().apply {
        add(actorTabViewController)
        addAll(actorTabViewController.modules)
    }

    override val systems: LinkedHashSet<System> =
        linkedSetOf()

    init {
        editorScreen2.actorTabView = actorTabViewController.view
    }
//    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        log.info { "Starting editor" }
        engine.ui.setScreen(editorScreen2)
        actorTabViewController.enableRenderer(engine.rendererManager)
//
//        engine.input.keyMouse.bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)
//
//        sceneTabController.init()
//        actorTabViewController.init()
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

//        if (event is PerformCommandEvent) {
//            execute(event.editorCommand)
//        } else if (event is SelectTabEvent) {
//            onSelectTab(event.tab)
//        }
    }

//    private fun onSelectTab(tab: Tab) {
//        if (tab == Tab.SCENE) {
//            engine.rendererManager.reset()
//        } else if (tab == Tab.ACTOR) {
//            actorTabViewController.enable()
//        }
//    }
//
//    fun handleEditorEvent(event: EditorEvent) {
//        log.debug { "Fired event ${event::class.simpleName}" }
//
//        when (editorScreen.activeTab) {
//            Tab.SCENE -> sceneTabController.handleEditorEvent(event)
//            Tab.ACTOR -> actorTabViewController.handleEditorEvent(event)
//            Tab.NONE -> error("No tab selected")
//        }
//
//        engine.events.fire(event)
//    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

//        when (editorScreen.activeTab) {
//            Tab.SCENE -> sceneTabController.update()
//            Tab.ACTOR -> actorTabViewController.update()
//            Tab.NONE -> error("No tab selected")
//        }
//
//        engine.input.onAction("openContextMenu") {
//            openContextMenu()
//        }
    }

//    private fun execute(command: EditorCommand) {
//        log.debug { "Executing ${command::class.simpleName}" }
//        command.execute()
//        commands.addFirst(command)
//        handleEvent(PerformActionEvent())
//    }
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
}
