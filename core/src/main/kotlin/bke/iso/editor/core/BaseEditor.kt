package bke.iso.editor.core

import bke.iso.editor.core.command.EditorCommand
import bke.iso.engine.core.Event
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World

abstract class BaseEditor {

    protected abstract val renderer: Renderer
    protected abstract val world: World

    protected val commands = EditorCommandStack()

    abstract fun start()
    abstract fun stop()
    abstract fun update()
    abstract fun handleEvent(event: Event)

    fun executeCommand(command: EditorCommand) {
        commands.execute(command)
    }
}
