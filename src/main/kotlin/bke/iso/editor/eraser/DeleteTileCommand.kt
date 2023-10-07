package bke.iso.editor.eraser

import bke.iso.editor.EditorCommand
import bke.iso.engine.math.Location
import bke.iso.engine.world.World

class DeleteTileCommand(
    private val world: World,
    private val location: Location
) : EditorCommand {

    override fun execute() {
        world.deleteTile(location)
    }

    override fun undo() {
        TODO("Not yet implemented")
    }
}
