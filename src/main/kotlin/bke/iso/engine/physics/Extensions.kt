package bke.iso.engine.physics

import bke.iso.engine.math.Box
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile

fun GameObject.getCollisionData(): CollisionData? =
    when (this) {
        is Tile -> getCollisionData()
        is Actor -> getCollisionData()
        else -> null
    }

fun Tile.getCollisionData() =
    CollisionData(
        Box(location.toVector3().add(0.5f, 0.5f, 0f), 1f, 1f, 0f),
        solid
    )

fun Actor.getCollisionData(): CollisionData? {
    val collision = components[Collider::class] ?: return null
    val bounds = collision.bounds
    return CollisionData(
        Box(
            pos.add(bounds.offset),
            bounds.dimensions.x,
            bounds.dimensions.y,
            bounds.dimensions.z
        ),
        collision.solid
    )
}