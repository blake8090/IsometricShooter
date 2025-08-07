package bke.iso.editor.core

abstract class EditorCommand {
    abstract val name: String

    abstract fun execute()

    abstract fun undo()

    open fun redo() {
        execute()
    }
}
