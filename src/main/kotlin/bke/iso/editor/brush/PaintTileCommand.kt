package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.editor.ReferenceActors
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.world.Actor

class PaintTileCommand(
    private val referenceActors: ReferenceActors,
    private val prefab: TilePrefab,
    private val location: Location
) : EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        referenceActors.deleteTile(location)
        actor = referenceActors.create(prefab, location)
    }

    override fun undo() {
        referenceActors.delete(actor)
    }
}
