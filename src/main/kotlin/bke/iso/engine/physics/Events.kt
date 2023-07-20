package bke.iso.engine.physics

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.Event

data class MoveEvent(
    val entity: Entity,
    val dx: Float,
    val dy: Float,
    val dz: Float,
    val speed: Float,
    val deltaTime: Float
) : Event()

data class CollisionEvent(
    val entity: Entity,
    // TODO: we don't need collision data (for now...)
    val collisionData: CollisionData
) : Event()
