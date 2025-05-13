package bke.iso.editor.entity.command

import bke.iso.editor.EditorCommand
import bke.iso.engine.world.entity.Component

data class DeleteComponentCommand(
    val components: MutableList<Component>,
    val component: Component
) : EditorCommand() {

    override val name: String = "DeleteComponent"

    override fun execute() {
        components.remove(component)
    }

    override fun undo() {
        components.add(component)
    }
}
