package bke.iso.editor.v3

abstract class EditorCommand {
    abstract val name: String

    abstract fun execute()

    abstract fun undo()
}
