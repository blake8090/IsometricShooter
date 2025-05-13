package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.world.entity.Actor
import com.badlogic.gdx.math.Vector3

data class PaintActorCommand(
    private val worldLogic: WorldLogic,
    private val prefab: ActorPrefab,
    private val pos: Vector3,
) : EditorCommand() {

    override val name: String = "PaintActor"

    private lateinit var actor: Actor

    override fun execute() {
        actor = worldLogic.createReferenceActor(prefab, pos)
    }

    override fun undo() {
        worldLogic.delete(actor)
    }
}
