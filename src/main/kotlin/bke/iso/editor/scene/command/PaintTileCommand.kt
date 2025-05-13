package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.world.entity.Entity

data class PaintTileCommand(
    private val worldLogic: WorldLogic,
    private val prefab: TilePrefab,
    private val location: Location
) : EditorCommand() {

    override val name: String = "PaintTile"

    private lateinit var entity: Entity

    override fun execute() {
        worldLogic.deleteTile(location)
        entity = worldLogic.createReferenceEntity(prefab, location)
    }

    override fun undo() {
        worldLogic.delete(entity)
    }
}
