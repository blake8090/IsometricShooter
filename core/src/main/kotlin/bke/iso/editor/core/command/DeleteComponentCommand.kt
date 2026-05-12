package bke.iso.editor.core.command

import bke.iso.engine.world.entity.Component

data class DeleteComponentCommand(
    val components: MutableList<Component>,
    val component: Component,
    val onActionCompleted: () -> Unit
) : EditorCommand() {

    override val name: String = "DeleteComponent"

    override fun execute() {
        components.remove(component)
        onActionCompleted.invoke()
    }

    override fun undo() {
        components.add(component)
        onActionCompleted.invoke()
    }
}
