package bke.iso.engine.collision

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("collider")
data class Collider (
    val size: Vector3,
    val offset: Vector3 = Vector3()
) : Component()
