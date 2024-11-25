package bke.iso.editorv2.scene.tool.eraser

import bke.iso.editorv2.EditorCommand
import bke.iso.editorv2.scene.ReferenceActorModule
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
