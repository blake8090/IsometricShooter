package bke.iso.engine.physics

import bke.iso.engine.math.Box2
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import com.badlogic.gdx.math.Vector3

data class CollisionData(
    val box: Box2,
    val solid: Boolean
)

fun GameObject.getCollisionData(): CollisionData? =
    when (this) {
        is Tile -> getCollisionData()
        is Actor -> getCollisionData()
        else -> null
    }

fun Tile.getCollisionData(): CollisionData {
    val min = location.toVector3()
    val max = Vector3(min).add(1f, 1f, 0f)
    return CollisionData(
        Box2.from(min, max),
        solid
    )
}

fun Actor.getCollisionData(): CollisionData? {
    val collider = get<Collider>() ?: return null
    val min = pos.add(collider.offset)
    val max = Vector3(min).add(collider.size)
    return CollisionData(
        Box2.from(min, max),
        collider.solid
    )
}