package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.prefab.EntityPrefab
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3

data class PaintEntityCommand(
    private val worldLogic: WorldLogic,
    private val prefab: EntityPrefab,
    private val pos: Vector3,
) : EditorCommand() {

    override val name: String = "PaintActor"

    private lateinit var entity: Entity

    override fun execute() {
        entity = worldLogic.createReferenceActor(prefab, pos)
    }

    override fun undo() {
        worldLogic.delete(entity)
    }
}
