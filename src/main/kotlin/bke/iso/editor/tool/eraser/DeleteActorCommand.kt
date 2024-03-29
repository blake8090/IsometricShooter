package bke.iso.editor.tool.eraser

import bke.iso.editor.tool.EditorCommand
import bke.iso.editor.ReferenceActors
import bke.iso.engine.world.actor.Actor

class DeleteActorCommand(
    private val referenceActors: ReferenceActors,
    private val actor: Actor
) : EditorCommand {

    override fun execute() {
        referenceActors.delete(actor)
    }

    override fun undo() {
        TODO()
    }
}
