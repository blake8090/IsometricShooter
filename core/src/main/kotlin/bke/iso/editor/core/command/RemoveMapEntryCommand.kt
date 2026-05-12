package bke.iso.editor.core.command

class RemoveMapEntryCommand(
    val map: MutableMap<Any, Any>,
    val key: Any
) : EditorCommand() {

    override val name = "RemoveMapEntry"

    private lateinit var removedValue: Any

    override fun execute() {
        removedValue = checkNotNull(map.remove(key)) {
            "Expected value for key $key"
        }
    }

    override fun undo() {
        map.put(key, removedValue)
    }
}
