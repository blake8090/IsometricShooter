package bke.iso.editorv2.scene.tool.brush

import bke.iso.editorv2.scene.ReferenceActorModule
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.world.actor.Actor

class PaintTileCommand(
    private val referenceActorModule: ReferenceActorModule,
    private val prefab: TilePrefab,
    private val location: Location
) : bke.iso.editorv2.EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        referenceActorModule.deleteTile(location)
        actor = referenceActorModule.create(prefab, location)
    }

    override fun undo() {
        referenceActorModule.delete(actor)
    }
}
