package bke.iso.editor.v3

import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorState3(override val engine: Engine) : State() {

    private val log = KotlinLogging.logger {}

    override val systems: LinkedHashSet<System> = linkedSetOf()
    override val modules: Set<Module> = emptySet()

    private val commands = ArrayDeque<EditorCommand>()

    override suspend fun load() {
        engine.ui2.setLayer(EditorLayer(engine))
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        if (event is OnExecuteCommand) {
            execute(event.command)
        }
    }

    private fun execute(command: EditorCommand) {
        log.debug { "Executing command: ${command.name}" }
        command.execute()
        commands.addFirst(command)
    }

    data class OnExecuteCommand(val command: EditorCommand) : Event
}
