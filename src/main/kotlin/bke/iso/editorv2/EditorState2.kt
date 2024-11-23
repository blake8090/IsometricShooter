package bke.iso.editorv2

import bke.iso.editor.event.EditorEvent
import bke.iso.editorv2.scene.EditorSceneTabController
import bke.iso.editorv2.ui.EditorScreen2
import bke.iso.engine.Game
import bke.iso.engine.state.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorState2(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val screen = EditorScreen2(this, game.assets)
    private val sceneTabController = EditorSceneTabController(game, screen)

    override val modules: Set<Module> =
        sceneTabController.getModules()

    override val systems: LinkedHashSet<System> =
        linkedSetOf()

    override suspend fun load() {
        log.info { "Starting editor" }
        game.ui.setScreen(screen)

        sceneTabController.init()
    }

    fun handleEvent(event: EditorEvent) {
        log.debug { "Fired event ${event::class.simpleName}" }
        game.events.fire(event)
    }
}
