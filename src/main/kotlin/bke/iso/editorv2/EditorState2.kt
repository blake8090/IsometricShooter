package bke.iso.editorv2

import bke.iso.editor.event.EditorEvent
import bke.iso.editorv2.scene.ReferenceActorModule
import bke.iso.editorv2.scene.SceneModule
import bke.iso.editorv2.ui.EditorScreen2
import bke.iso.engine.Game
import bke.iso.engine.state.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorState2(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val referenceActorModule = ReferenceActorModule(game.world)

    private val sceneModule = SceneModule(
        game.dialogs,
        game.serializer,
        game.world,
        game.assets,
        referenceActorModule
    )

    override val modules: Set<Module> = setOf(
        referenceActorModule,
        sceneModule
    )

    override val systems: LinkedHashSet<System> = linkedSetOf()

    private val screen = EditorScreen2(this, game.assets)

    override suspend fun load() {
        log.info { "Starting editor" }

        game.ui.setScreen(screen)
    }

    fun handleEvent(event: EditorEvent) {
        log.debug { "Fired event ${event::class.simpleName}" }
        game.events.fire(event)
    }
}
