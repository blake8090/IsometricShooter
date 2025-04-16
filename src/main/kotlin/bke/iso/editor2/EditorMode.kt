package bke.iso.editor2

import bke.iso.engine.core.Event
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World

abstract class EditorMode {

    protected abstract val renderer: Renderer
    protected abstract val world: World

    private var commands = mutableListOf<EditorCommand>()
    private var pointer = -1

    abstract fun start()
    abstract fun stop()
    abstract fun update()
    abstract fun draw()
    abstract fun handleEvent(event: Event)

    fun execute(command: EditorCommand) {
        command.execute()
        pointer++
        commands.add(pointer, command)

        // truncate old commands if necessary
        if (commands.size - 1 > pointer) {
            commands = commands.subList(0, pointer + 1)
        }
    }

    protected fun undo() {
        if (pointer < 0) {
            return
        }
        commands[pointer].undo()
        pointer--
    }

    protected fun redo() {
        if (pointer >= commands.size - 1) {
            return
        }
        pointer++
        commands[pointer].execute()
    }

    protected fun resetCommands() {
        commands.clear()
        pointer = -1
    }
}
