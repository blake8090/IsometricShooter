package bke.iso.editor.core

class EditorCommandStack {

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
        commands[pointer].undo()
        pointer--
    }

    fun redo() {
        if (pointer >= commands.size - 1) {
            return
        }
        pointer++
        commands[pointer].execute()
    }

    fun reset() {
        commands.clear()
        pointer = -1
    }
}
