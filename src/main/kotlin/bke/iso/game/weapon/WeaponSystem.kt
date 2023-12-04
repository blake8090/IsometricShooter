package bke.iso.game.weapon

import bke.iso.engine.System
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import kotlin.math.max

class WeaponSystem(private val world: World) : System {

    override fun update(deltaTime: Float) =
        world.actors.each { _: Actor, equippedWeapon: EquippedWeapon ->
            equippedWeapon.coolDown = max(0f, equippedWeapon.coolDown - deltaTime)
            equippedWeapon.recoil = max(0f, equippedWeapon.recoil - deltaTime)
        }
}
