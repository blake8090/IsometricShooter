package bke.iso.game.weapon

import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("bullet")
data class Bullet(
    val shooterId: String,
    val damage: Float,
    @Contextual
    val start: Vector3
) : Component

/**
 * Provides an offset for projectiles when shooting a ranged weapon, relative to an actor's position.
 */
data class RangedWeaponOffset(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) : Component

interface Weapon {
    val name: String
}

data class RangedWeapon(
    override val name: String,
    var ammo: Int = 0,
    var coolDown: Float = 0f,
    var reloadCoolDown: Float = 0f,
    var recoil: Float = 0f
) : Weapon

data class Inventory(
    var selectedWeapon: Weapon? = null
) : Component
