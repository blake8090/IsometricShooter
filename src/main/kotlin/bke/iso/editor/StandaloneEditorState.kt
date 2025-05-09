package bke.iso.editor

import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class StandaloneEditorState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = LinkedHashSet()

    // TODO: modules shouldn't need the whole engine as a param
    override val modules: Set<Module> = setOf(EditorModule(engine))

    private val log = KotlinLogging.logger {}

    override suspend fun load() {
        log.info { "Starting editor" }
        engine.ui.clearScene2dViews()
        engine.ui.clearImGuiViews()
        engine.events.fire(EditorModule.SceneModeSelected())
    }
}
