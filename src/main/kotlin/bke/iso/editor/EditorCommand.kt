package bke.iso.editor

interface EditorCommand {
    fun execute()
    fun undo()
}
