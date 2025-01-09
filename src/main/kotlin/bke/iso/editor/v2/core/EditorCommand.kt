package bke.iso.editor.v2.core

abstract class EditorCommand {
    abstract val name: String

    abstract fun execute()

    abstract fun undo()
}
