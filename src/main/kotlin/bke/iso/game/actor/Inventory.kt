package bke.iso.game.actor

import bke.iso.engine.world.actor.Component
import bke.iso.game.weapon.Weapon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("inventory")
data class Inventory(
    var selectedWeapon: Weapon? = null,
    var numMedkits: Int = 0
) : Component
