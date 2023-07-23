package bke.iso.engine.physics

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.Event
import com.badlogic.gdx.math.Vector3

data class MoveEvent(
    val entity: Entity,
    val delta: Vector3,
    val speed: Vector3,
    val deltaTime: Float
) : Event()

data class CollisionEvent(
    val entity: Entity,
    // TODO: we don't need collision data (for now...)
    val collisionData: CollisionData
) : Event()
