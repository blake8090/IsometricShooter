package bke.iso.game.weapon

import bke.iso.engine.asset.Assets
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

class Weapons(
    private val assets: Assets,
    world: World
) {

    private val map = mutableMapOf<String, WeaponLogic>()

    init {
        val rangedWeaponLogic = RangedWeaponLogic(world)
        map["pistol"] = rangedWeaponLogic
        map["rifle"] = rangedWeaponLogic
    }

    fun equip(actor: Actor, name: String) {
        val inventory = actor.getOrPut(Inventory())
        val properties = assets.get<WeaponProperties>(name)
        if (properties is RangedWeaponProperties) {
            val weaponItem = RangedWeaponItem(name, properties.magSize)
            inventory.selectedWeapon = weaponItem
        }
    }

    fun shoot(actor: Actor, target: Vector3) {
        val weaponItem = actor.get<Inventory>()
            ?.selectedWeapon
            ?: return

        if (weaponItem is RangedWeaponItem) {
            shootRangedWeapon(actor, target, weaponItem)
        }
    }

    private fun shootRangedWeapon(actor: Actor, target: Vector3, weaponItem: RangedWeaponItem) {
        val name = weaponItem.name
        val properties = assets.get<WeaponProperties>(name) as RangedWeaponProperties
        val logic = checkNotNull(map[name] as? RangedWeaponLogic) {
            "Ranged weapon '$name' not found"
        }
        logic.shoot(actor, target, weaponItem, properties)
    }

    fun reload(weapon: RangedWeaponItem) {
        val name = weapon.name
        val properties = assets.get<WeaponProperties>(name) as RangedWeaponProperties
        val logic = checkNotNull(map[name] as? RangedWeaponLogic) {
            "Ranged weapon '$name' not found"
        }
        logic.reload(weapon, properties)
    }
}
