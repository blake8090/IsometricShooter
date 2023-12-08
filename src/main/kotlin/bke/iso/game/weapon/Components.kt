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
    val offset: Vector3 = Vector3(),
    var coolDown: Float = 0f,
    var recoil: Float = 0f
) : WeaponItem()

data class Inventory(
    var selectedWeapon: WeaponItem? = null
) : Component
