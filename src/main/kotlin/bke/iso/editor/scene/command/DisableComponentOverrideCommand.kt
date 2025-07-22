package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity

data class DisableComponentOverrideCommand(
    val worldLogic: WorldLogic,
    val referenceEntity: Entity,
    val componentOverride: Component,
) : EditorCommand() {

    override val name: String = "DisableComponentOverride"

    override fun execute() {
        val data = worldLogic.getData(referenceEntity)
        val type = componentOverride::class.simpleName

        check(data.template.components.any { c -> c::class == componentOverride::class }) {
            "Template ${data.template.name} for reference entity $referenceEntity does not have a component of type $type"
        }

        check(data.componentOverrides.any { c -> c::class == componentOverride::class }) {
            "Reference entity $referenceEntity does not have a component override of type $type"
        }

        check(data.componentOverrides.remove(componentOverride)) {
            "Could not remove component $type from reference entity $referenceEntity componentOverrides"
        }

        worldLogic.refreshComponents(referenceEntity)
    }

    override fun undo() {
        worldLogic
            .getData(referenceEntity)
            .componentOverrides
            .add(componentOverride)

        worldLogic.refreshComponents(referenceEntity)
    }
}
