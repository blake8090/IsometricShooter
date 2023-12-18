package bke.iso.editor.tool.brush

import bke.iso.editor.tool.EditorCommand
import bke.iso.editor.ReferenceActors
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.world.Actor
import com.badlogic.gdx.math.Vector3

class PaintActorCommand(
    private val referenceActors: ReferenceActors,
    private val prefab: ActorPrefab,
    private val pos: Vector3
) : EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        actor = referenceActors.create(prefab, pos)
    }

    override fun undo() {
        referenceActors.delete(actor)
    }
}
