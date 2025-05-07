package bke.iso.editor

abstract class EditorCommand {
    abstract val name: String

    abstract fun execute()

    abstract fun undo()
}
