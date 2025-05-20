package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.world.entity.Entity

data class AssignBuildingCommand(
    val entity: Entity,
    val building: String?,
    val worldLogic: WorldLogic
) : EditorCommand() {

    override val name: String = "AssignBuilding"

    private var previousBuilding: String? = null

    override fun execute() {
        previousBuilding = worldLogic.getBuilding(entity)
        worldLogic.setBuilding(entity, building)
    }

    override fun undo() {
        worldLogic.setBuilding(entity, previousBuilding)
    }
}
