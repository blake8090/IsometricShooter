package bke.iso.game.weapon

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("bullet2")
data class Bullet(
    val shooterId: String,
    val damage: Float,
    @Contextual
    val start: Vector3
) : Component

// TODO: rename to Weapon?
sealed class WeaponItem {
    abstract val name: String
}

data class RangedWeaponItem(
    override val name: String,
    var ammo: Int = 0,
    var coolDown: Float = 0f,
    var reloadCoolDown: Float = 0f,
    var recoil: Float = 0f
) : WeaponItem()

data class Inventory(
    var selectedWeapon: WeaponItem? = null
) : Component

/**
 * Provides an offset for projectiles when shooting a ranged weapon, relative to an actor's position.
 */
data class RangedWeaponOffset(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) : Component
