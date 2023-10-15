package bke.iso.editor

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.engine.withFirstInstance
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

data class ActorPrefabReference(val prefab: String) : Component

data class TilePrefabReference(val prefab: String) : Component

class ReferenceActors(private val world: World) {

    private val tileLocations = mutableSetOf<Location>()

    fun hasTile(location: Location): Boolean =
        tileLocations.contains(location)

    fun clear() {
        tileLocations.clear()
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

        return world.actors.create(pos.x, pos.y, pos.z, *components.toTypedArray())
    }

    fun create(prefab: TilePrefab, location: Location): Actor {
        if (!tileLocations.add(location)) {
            error("Duplicate tile at location $location")
        }

        return world.actors.create(
            location,
            Sprite(prefab.texture, 0f, 16f),
            TilePrefabReference(prefab.name),
            Collider(Vector3(1f, 1f, 0f))
        )
    }

    fun delete(actor: Actor) {
        if (actor.has<TilePrefabReference>()) {
            tileLocations.remove(Location(actor.pos))
        }
        world.actors.delete(actor)
    }
}
