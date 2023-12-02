package bke.iso.game.weapon

import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.Collider
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Description
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

class Weapons(
    private val assets: Assets,
    private val world: World
) {

    fun equip(actor: Actor, name: String): EquippedWeapon {
        val weapon = assets.get<Weapon>(name)
        val equippedWeapon = EquippedWeapon(name, weapon.magSize)
        actor.add(equippedWeapon)
        return equippedWeapon
    }

    fun shoot(shooter: Actor, pos: Vector3, target: Vector3) {
        val equippedWeapon = shooter.get<EquippedWeapon>()
        if (equippedWeapon == null || equippedWeapon.ammmo <= 0f || equippedWeapon.coolDown > 0f) {
            return
        }

        val weapon = assets.get<Weapon>(equippedWeapon.name)
        val velocity = Vector3(target)
            .sub(pos)
            .nor()
            .scl(weapon.bulletVelocity)

        world.actors.create(
            pos,
            Bullet(shooter.id, weapon.damage, pos),
            Sprite("bullet.png", 8f, 8f),
            PhysicsBody(PhysicsMode.GHOST, velocity),
            Collider(
                Vector3(0.125f, 0.125f, 0.125f),
                Vector3(0f, -0.125f, 0f)
            ),
            DebugSettings().apply {
                zAxis = false
            },
            Description("bullet")
        )

        equippedWeapon.ammmo--
        equippedWeapon.coolDown = calculateCoolDown(weapon)
    }

    private fun calculateCoolDown(weapon: Weapon): Float {
        val roundsPerMinute = weapon.fireRate
        val roundsPerSecond = roundsPerMinute / 60f
        return 1 / roundsPerSecond
    }
}
