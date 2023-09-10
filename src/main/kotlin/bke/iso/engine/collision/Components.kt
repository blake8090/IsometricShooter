package bke.iso.engine.collision

import bke.iso.engine.world.Component
import bke.iso.engine.world.ComponentSubType
import com.badlogic.gdx.math.Vector3

@ComponentSubType("collider")
data class Collider (
    val size: Vector3,
    val offset: Vector3 = Vector3()
) : Component()
