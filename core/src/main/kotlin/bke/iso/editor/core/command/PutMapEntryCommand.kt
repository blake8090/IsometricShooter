package bke.iso.editor.core.command

class PutMapEntryCommand(
    val map: MutableMap<Any, Any>,
    val key: Any,
    val value: Any
) : EditorCommand() {

    override val name = "PutMapEntry"

    private var newEntry = false
    private var previousValue: Any? = null

    override fun execute() {
        if (!map.contains(key)) {
            newEntry = true
        } else {
            previousValue = map[key]
        }
        map.put(key, value)
    }

    override fun undo() {
        if (newEntry) {
            map.remove(key)
        } else if (previousValue != null) {
            map.put(key, previousValue!!)
        }
    }

    override fun redo() {
        map.put(key, value)
    }
}
