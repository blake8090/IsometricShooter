package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
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
        actor = world.actors.create(pos.x, pos.y, pos.z,)
        prefab
            .components
            .filterIsInstance<Sprite>()
            .firstOrNull()
            ?.let { sprite -> actor.add(sprite.copy()) }
        prefab
            .components
            .filterIsInstance<Collider>()
            .firstOrNull()
            ?.let { collider -> actor.add(collider.copy()) }
    }

    override fun undo() {
        world.actors.delete(actor)
    }
}
