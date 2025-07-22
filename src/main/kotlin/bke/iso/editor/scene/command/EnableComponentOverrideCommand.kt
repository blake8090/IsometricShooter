package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity

data class EnableComponentOverrideCommand(
    val serializer: Serializer,
    val worldLogic: WorldLogic,
    val referenceEntity: Entity,
    val templateComponent: Component
) : EditorCommand() {

    override val name: String = "EnableComponentOverride"

    override fun execute() {
        val data = worldLogic.getData(referenceEntity)
        check(data.componentOverrides.none { c -> c::class == templateComponent::class }) {
            "EntityData for reference entity $referenceEntity already has component override for ${templateComponent::class.simpleName}"
        }
        data.componentOverrides.add(copy(templateComponent))

        worldLogic.refreshComponents(referenceEntity)
    }

    private inline fun <reified T : Component> copy(component: T): T {
        val content = serializer.write(component)
        return serializer.read<T>(content)
    }

    override fun undo() {
        val data = worldLogic.getData(referenceEntity)
        check(data.componentOverrides.any { c -> c::class == templateComponent::class }) {
            "Expected reference entity $referenceEntity to have a component override for ${templateComponent::class.simpleName}"
        }
        data.componentOverrides.removeIf { c -> c::class == templateComponent::class }

        worldLogic.refreshComponents(referenceEntity)
    }
}
