package bke.iso.game.entity

import bke.iso.engine.world.entity.Component
import bke.iso.game.weapon.system.Weapon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("inventory")
data class Inventory(
    var selectedWeapon: Weapon? = null,
    var numMedkits: Int = 0
) : Component
