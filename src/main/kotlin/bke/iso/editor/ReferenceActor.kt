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

fun createReferenceActor(world: World, pos: Vector3, prefab: ActorPrefab): Actor {
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

fun createReferenceActor(world: World, location: Location, prefab: TilePrefab): Actor =
    world.actors.create(
        location,
        Sprite(prefab.texture, 0f, 16f),
        TilePrefabReference(prefab.name),
        Collider(Vector3(1f, 1f, 0f))
    )
