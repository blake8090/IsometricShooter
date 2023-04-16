package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Component
import com.badlogic.gdx.math.Vector3

data class BoundsV2(
    val dimensions: Vector3,
    val offset: Vector3 = Vector3()
)

data class CollisionV2(
    val bounds: BoundsV2,
    val solid: Boolean
) : Component()
