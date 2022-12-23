package bke.iso.engine.physics

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.Event

data class MoveEvent(
    val entity: Entity,
    val dx: Float,
    val dy: Float
) : Event()

data class CollisionEvent(
    val entity: Entity,
    val collisionData: CollisionData
) : Event()
