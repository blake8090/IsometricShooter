package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.world.entity.Actor

data class AssignBuildingCommand(
    val actor: Actor,
    val building: String?,
    val worldLogic: WorldLogic
) : EditorCommand() {

    override val name: String = "AssignBuilding"

    private var previousBuilding: String? = null

    override fun execute() {
        previousBuilding = worldLogic.getBuilding(actor)
        worldLogic.setBuilding(actor, building)
    }

    override fun undo() {
        worldLogic.setBuilding(actor, previousBuilding)
    }
}
