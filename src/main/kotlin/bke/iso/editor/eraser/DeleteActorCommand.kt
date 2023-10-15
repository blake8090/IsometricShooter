package bke.iso.editor.eraser

import bke.iso.editor.EditorCommand
import bke.iso.editor.ReferenceActors
import bke.iso.engine.world.Actor

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
