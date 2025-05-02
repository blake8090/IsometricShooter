package bke.iso.editor2.scene.command

import bke.iso.editor2.EditorCommand
import bke.iso.editor2.scene.WorldLogic
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.world.actor.Actor

data class PaintTileCommand(
    private val worldLogic: WorldLogic,
    private val prefab: TilePrefab,
    private val location: Location
) : EditorCommand() {

    override val name: String = "PaintTile"

    private lateinit var actor: Actor

    override fun execute() {
        worldLogic.deleteTile(location)
        actor = worldLogic.createReferenceActor(prefab, location)
    }

    override fun undo() {
        worldLogic.delete(actor)
    }
}
