package bke.iso.editor.scene.command

import bke.iso.editor.core.command.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.world.entity.Entity

data class DeleteEntityCommand(
    private val worldLogic: WorldLogic,
    private val entity: Entity
) : EditorCommand() {

    override val name: String = "DeleteEntity"

    override fun execute() {
        worldLogic.delete(entity)
    }

    override fun undo() {
        worldLogic.add(entity)
    }
}
