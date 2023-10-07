package bke.iso.editor.eraser

import bke.iso.editor.EditorCommand
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World

class DeleteActorCommand(
    private val world: World,
    private val actor: Actor
) : EditorCommand {

    override fun execute() {
        world.actors.delete(actor)
    }

    override fun undo() {
        TODO()
    }
}
