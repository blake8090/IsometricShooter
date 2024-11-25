package bke.iso.editor.scene.tool.brush

import bke.iso.editor.scene.ReferenceActorModule
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.math.Vector3

class PaintActorCommand(
    private val referenceActorModule: ReferenceActorModule,
    private val prefab: ActorPrefab,
    private val pos: Vector3,
) : bke.iso.editor.EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        actor = referenceActorModule.create(prefab, pos)
    }

    override fun undo() {
        referenceActorModule.delete(actor)
    }
}
