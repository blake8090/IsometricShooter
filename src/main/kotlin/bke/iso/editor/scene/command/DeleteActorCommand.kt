package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.world.actor.Actor

data class DeleteActorCommand(
    private val worldLogic: WorldLogic,
    private val actor: Actor
) : EditorCommand() {

    override val name: String = "DeleteActor"

    override fun execute() {
        worldLogic.delete(actor)
    }

    override fun undo() {
        worldLogic.add(actor)
    }
}
