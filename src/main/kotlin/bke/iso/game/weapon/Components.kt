package bke.iso.game.weapon

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("equippedWeapon")
data class EquippedWeapon(
    val name: String,
    var ammmo: Int,
    var coolDown: Float = 0f,
    var recoil: Float = 0f
) : Component

@Serializable
@SerialName("bullet2")
data class Bullet(
    val shooterId: String,
    val damage: Float,
    @Contextual
    val start: Vector3
) : Component
