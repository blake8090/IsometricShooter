package bke.iso.game.actor

import bke.iso.engine.world.actor.Component
import bke.iso.game.weapon.Weapon

data class Inventory(
    var selectedWeapon: Weapon? = null,
    var numMedkits: Int = 0
) : Component
