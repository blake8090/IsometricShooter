package bke.iso.editor2.scene.command

import bke.iso.editor2.EditorCommand
import bke.iso.editor2.scene.WorldLogic
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
