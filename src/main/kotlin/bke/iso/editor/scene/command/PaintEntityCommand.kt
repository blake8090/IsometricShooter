package bke.iso.editor.scene.command

import bke.iso.editor.core.command.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.asset.entity.has
import bke.iso.engine.math.Location
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3

data class PaintEntityCommand(
    private val worldLogic: WorldLogic,
    private val template: EntityTemplate,
    private val pos: Vector3,
) : EditorCommand() {

    override val name: String = "PaintEntity"

    private lateinit var entity: Entity
    private var replacedTile: Entity? = null

    override fun execute() {
        if (template.has<Tile>()) {
            replaceTile(Location(pos))
        }

        entity = worldLogic.createReferenceEntity(template, pos)
    }

    private fun replaceTile(location: Location) {
        val tileEntity = worldLogic.getTileEntity(location) ?: return
        worldLogic.delete(tileEntity)
        replacedTile = tileEntity
    }

    override fun undo() {
        worldLogic.delete(entity)
        if (replacedTile != null) {
            worldLogic.add(replacedTile!!)
        }
    }

    override fun redo() {
        if (replacedTile != null) {
            worldLogic.delete(replacedTile!!)
        }
        worldLogic.add(entity)
    }
}
