package bke.iso.editor.scene.tool.eraser

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.ReferenceActorModule
import bke.iso.engine.world.actor.Actor

class DeleteActorCommand(
    private val referenceActorModule: ReferenceActorModule,
    private val actor: Actor
) : EditorCommand {

    override fun execute() {
        referenceActorModule.delete(actor)
    }

    override fun undo() {
        TODO()
    }
}
