package bke.iso.game.weapon

import bke.iso.engine.System
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import kotlin.math.max

class WeaponSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actors.each { _: Actor, inventory: Inventory ->
            val weaponItem = inventory.selectedWeapon
            if (weaponItem is RangedWeaponItem) {
                weaponItem.coolDown = max(0f, weaponItem.coolDown - deltaTime)
                weaponItem.recoil = max(0f, weaponItem.recoil - deltaTime)
            }
        }
    }
}
