package bke.iso.editor.entity.command

import bke.iso.editor.core.EditorCommand
import bke.iso.engine.world.entity.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

data class AddComponentCommand(
    val components: MutableList<Component>,
    val componentType: KClass<out Component>,
    val onActionCompleted: () -> Unit
) : EditorCommand() {

    override val name = "AddComponent"

    private var addedComponent: Component? = null

    override fun execute() {
        val newComponent = componentType.createInstance()
        components.add(newComponent)
        addedComponent = newComponent
        onActionCompleted.invoke()
    }

    override fun undo() {
        val component = checkNotNull(addedComponent) {
            "Expected an added component"
        }
        components.remove(component)
        onActionCompleted.invoke()
    }
}
