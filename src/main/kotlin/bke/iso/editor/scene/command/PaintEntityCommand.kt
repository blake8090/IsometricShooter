package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3

data class PaintEntityCommand(
    private val worldLogic: WorldLogic,
    private val template: EntityTemplate,
    private val pos: Vector3,
) : EditorCommand() {

    override val name: String = "PaintEntity"

    private lateinit var entity: Entity

    override fun execute() {
        entity = worldLogic.createReferenceEntity(template, pos)
    }

    override fun undo() {
        worldLogic.delete(entity)
    }
}
