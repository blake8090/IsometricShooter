package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.EntityTemplateReference
import bke.iso.engine.world.entity.Component

data class DisableComponentOverrideCommand(
    val templateReference: EntityTemplateReference,
    val componentOverride: Component
) : EditorCommand() {

    override val name: String = "EnableComponentOverride"

    override fun execute() {
        check(templateReference.componentOverrides.contains(componentOverride)) {
            "Expected componentOverrides to contain component ${componentOverride::class.simpleName}"
        }
        templateReference.componentOverrides.remove(componentOverride)
    }

    override fun undo() {
        check(templateReference.componentOverrides.add(componentOverride)) {
            "Could not add component ${componentOverride::class.simpleName} to componentOverrides"
        }
    }
}
