package bke.iso.game.weapon.system

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("bullet")
data class Bullet(
    val shooterId: String = "",
    val damage: Float = 0f,
    val range: Float = 0f,
    @Contextual
    val start: Vector3 = Vector3()
) : Component

/**
 * Provides an offset for projectiles when shooting a ranged weapon, relative to an entity's position.
 */
@Serializable
@SerialName("rangedWeaponOffset")
data class RangedWeaponOffset(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) : Component

interface Weapon {
    val name: String
}

@Serializable
@SerialName("rangedWeapon")
data class RangedWeapon(
    override val name: String,
    var ammo: Int = 0,
    var coolDown: Float = 0f,
    var reloadCoolDown: Float = 0f,
    var recoil: Float = 0f
) : Weapon

@Serializable
@SerialName("explosion")
data class Explosion(
    val timeSeconds: Float = 0f,
    var timer: Float = timeSeconds
) : Component

@Serializable
@SerialName("weaponPickup")
data class WeaponPickup(val name: String = "") : Component
