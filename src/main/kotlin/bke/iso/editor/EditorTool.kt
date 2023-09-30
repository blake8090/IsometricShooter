package bke.iso.editor

interface EditorTool {
    fun update()
    fun performAction(): EditorCommand?
    fun enable()
    fun disable()
}
