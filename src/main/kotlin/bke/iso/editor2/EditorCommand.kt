package bke.iso.editor2

abstract class EditorCommand {
    abstract val name: String

    abstract fun execute()

    abstract fun undo()
}
