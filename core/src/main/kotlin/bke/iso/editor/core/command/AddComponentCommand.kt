package bke.iso.editor.core.command

import bke.iso.engine.world.entity.Component

data class AddComponentCommand(
    val components: MutableList<Component>,
    val component: Component,
    val onActionCompleted: () -> Unit
) : EditorCommand() {

    override val name = "AddComponent"

    override fun execute() {
        components.add(component)
        onActionCompleted.invoke()
    }

    override fun undo() {
        components.remove(component)
        onActionCompleted.invoke()
    }
}
