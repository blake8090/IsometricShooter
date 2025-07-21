package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.EntityTemplateReference
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.entity.Component

data class EnableComponentOverrideCommand(
    val serializer: Serializer,
    val templateReference: EntityTemplateReference,
    val templateComponent: Component
) : EditorCommand() {

    override val name: String = "EnableComponentOverride"

    private lateinit var newComponent: Component

    override fun execute() {
        newComponent = copy(templateComponent)
        templateReference.componentOverrides.add(newComponent)
    }

    private inline fun <reified T : Component> copy(component: T): T {
        val content = serializer.write(component)
        return serializer.read<T>(content)
    }

    override fun undo() {
        templateReference.componentOverrides.remove(newComponent)
    }
}
