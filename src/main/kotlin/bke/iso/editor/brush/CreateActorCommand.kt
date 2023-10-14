package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.editor.createReferenceActor
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

class CreateActorCommand(
    private val world: World,
    private val prefab: ActorPrefab,
    private val pos: Vector3
) : EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        actor = createReferenceActor(world, pos, prefab)
    }

    override fun undo() {
        world.actors.delete(actor)
    }
}
