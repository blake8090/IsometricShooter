package bke.iso.editor.core

import io.github.oshai.kotlinlogging.KotlinLogging

class EditorCommandStack {

    private val log = KotlinLogging.logger { }

    private var commands = mutableListOf<EditorCommand>()
    private var pointer = -1

    fun execute(command: EditorCommand) {
        command.execute()
        pointer++
        commands.add(pointer, command)

        // truncate old commands if necessary
        if (commands.size - 1 > pointer) {
            commands = commands.subList(0, pointer + 1)
        }
    }

    fun undo() {
        if (pointer < 0) {
            return
        }

        val command = commands[pointer]
        log.debug { "Undoing command ${command.name}" }
        command.undo()
        pointer--
    }

    fun redo() {
        if (pointer >= commands.size - 1) {
            return
        }
        pointer++

        val command = commands[pointer]
        log.debug { "Redoing command ${command.name}" }
        command.redo()
    }

    fun reset() {
        commands.clear()
        pointer = -1
    }
}
