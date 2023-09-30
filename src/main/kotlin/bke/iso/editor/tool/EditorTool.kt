package bke.iso.editor.tool

interface EditorTool {
    fun update()
    fun performAction()
    fun enable()
    fun disable()
}
