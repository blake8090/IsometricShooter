package bke.iso.game.weapon.system

import bke.iso.engine.state.System
import bke.iso.engine.asset.Assets
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.actor.Inventory
import bke.iso.game.weapon.RangedWeapon
import bke.iso.game.weapon.RangedWeaponProperties
import bke.iso.game.weapon.WeaponProperties
import mu.KotlinLogging
import kotlin.math.max

class WeaponSystem(
    private val world: World,
    private val assets: Assets
) : System {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        world.actors.each { _: Actor, inventory: Inventory ->
            update(inventory, deltaTime)
        }
    }

    private fun update(inventory: Inventory, deltaTime: Float) {
        val weapon = inventory.selectedWeapon
        if (weapon is RangedWeapon) {
            weapon.coolDown = max(0f, weapon.coolDown - deltaTime)
            weapon.recoil = max(0f, weapon.recoil - deltaTime)

            if (weapon.reloadCoolDown > 0f) {
                reloadWeapon(weapon, deltaTime)
            }
        }
    }

    private fun reloadWeapon(weapon: RangedWeapon, deltaTime: Float) {
        weapon.reloadCoolDown = max(0f, weapon.reloadCoolDown - deltaTime)

        if (weapon.reloadCoolDown == 0f) {
            val properties = assets.get<WeaponProperties>(weapon.name) as RangedWeaponProperties
            weapon.ammo = properties.magSize
            log.debug { "reload finished" }
        }
    }
}
