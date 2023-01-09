package bke.iso.v2.engine.physics

import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.event.Event

data class MoveEvent(
    val entity: Entity,
    val dx: Float,
    val dy: Float,
    val speed: Float,
    val deltaTime: Float
) : Event()

data class CollisionEvent(
    val entity: Entity,
    // TODO: we don't need collision data (for now...)
    val collisionData: CollisionData
) : Event()
