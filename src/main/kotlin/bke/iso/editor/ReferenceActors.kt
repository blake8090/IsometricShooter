package bke.iso.editor

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Location
import bke.iso.engine.render.Occlude
import bke.iso.engine.render.Sprite
import bke.iso.engine.withFirstInstance
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

data class ActorPrefabReference(val prefab: String) : Component

data class TilePrefabReference(val prefab: String) : Component

// TODO: make into a module?
class ReferenceActors(private val world: World) {

    private val tilesByLocation = mutableMapOf<Location, Actor>()

    fun clear() {
        tilesByLocation.clear()
    }

    fun create(prefab: ActorPrefab, pos: Vector3): Actor {
        val components = mutableSetOf<Component>()
        components.add(ActorPrefabReference(prefab.name))

        prefab.components.withFirstInstance<Sprite> { sprite ->
            components.add(sprite.copy())
        }

        prefab.components.withFirstInstance<Collider> { collider ->
            components.add(collider.copy())
        }

        if (prefab.components.any { component -> component is Occlude }) {
            components.add(Occlude())
        }

        return world.actors.create(pos, *components.toTypedArray())
    }

    fun create(prefab: TilePrefab, location: Location): Actor {
        if (tilesByLocation.containsKey(location)) {
            error("Duplicate tile at location $location")
        }

        val actor = world.actors.create(
            location,
            prefab.sprite.copy(),
            TilePrefabReference(prefab.name),
            Collider(Vector3(1f, 1f, 0f))
        )
        tilesByLocation[location] = actor
        return actor
    }

    fun getTilePrefabName(location: Location): String? {
        val actor = tilesByLocation[location] ?: return null
        val reference = checkNotNull(actor.get<TilePrefabReference>()) {
            "Expected TilePrefabReference for actor $actor"
        }
        return reference.prefab
    }

    fun delete(actor: Actor) {
        if (actor.has<TilePrefabReference>()) {
            tilesByLocation.remove(Location(actor.pos))
        }
        world.actors.delete(actor)
    }

    fun deleteTile(location: Location) {
        val actor = tilesByLocation[location] ?: return
        delete(actor)
    }
}
