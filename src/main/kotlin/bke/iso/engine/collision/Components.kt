package bke.iso.engine.collision

import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("collider")
data class Collider(
    @Contextual
    val size: Vector3 = Vector3(),
    @Contextual
    val offset: Vector3 = Vector3()
) : Component
