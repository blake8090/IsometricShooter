package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.withFirstInstance
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

class CreateActorCommand(
    private val world: World,
    private val prefab: ActorPrefab,
    private val pos: Vector3
) : EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        val components = mutableSetOf<Component>()

        prefab.components.withFirstInstance<Sprite> { sprite ->
            components.add(sprite.copy())
        }

        prefab.components.withFirstInstance<Collider> { collider ->
            components.add(collider.copy())
        }

        actor = world.actors.create(pos.x, pos.y, pos.z, *components.toTypedArray())
    }

    override fun undo() {
        world.actors.delete(actor)
    }
}
