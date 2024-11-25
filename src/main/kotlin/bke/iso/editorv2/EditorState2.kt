package bke.iso.editorv2

import bke.iso.editor.event.EditorEvent
import bke.iso.editorv2.scene.SceneTabViewController
import bke.iso.editorv2.ui.EditorScreen
import bke.iso.engine.Game
import bke.iso.engine.state.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorState2(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)
    private val sceneTabController = SceneTabViewController(game, editorScreen.sceneTabView)

    override val modules: Set<Module> =
        sceneTabController.getModules()

    override val systems: LinkedHashSet<System> =
        linkedSetOf()

    override suspend fun load() {
        log.info { "Starting editor" }
        game.ui.setScreen(editorScreen)

        sceneTabController.init()
    }

    fun handleEvent(event: EditorEvent) {
        log.debug { "Fired event ${event::class.simpleName}" }
        game.events.fire(event)
    }
}
