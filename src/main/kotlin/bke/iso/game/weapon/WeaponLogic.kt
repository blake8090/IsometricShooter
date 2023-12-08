package bke.iso.game.weapon

import bke.iso.engine.collision.Collider
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import kotlin.random.Random.Default.nextDouble

interface WeaponLogic

class RangedWeaponLogic(private val world: World) : WeaponLogic {

    fun shoot(
        shooter: Actor,
        target: Vector3,
        selectedWeapon: RangedWeaponItem,
        properties: RangedWeaponProperties
    ) {
        println("shooting")
        if (selectedWeapon.ammo <= 0 || selectedWeapon.coolDown > 0f) {
            return
        }

        val start = shooter.pos.add(selectedWeapon.offset)
        applyRecoil(target, properties.recoil)
        val velocity = Vector3(target)
            .sub(start)
            .nor()
            .scl(properties.bulletVelocity)

        world.actors.create(
            start,
            Bullet(shooter.id, properties.damage, start),
            Sprite("bullet.png", 8f, 8f),
            PhysicsBody(PhysicsMode.GHOST, velocity),
            Collider(
                Vector3(0.125f, 0.125f, 0.125f),
                Vector3(0f, -0.125f, 0f)
            ),
            DebugSettings().apply {
                zAxis = false
            }
        )

        selectedWeapon.ammo--
        selectedWeapon.coolDown = calculateCoolDown(properties)
        selectedWeapon.recoil += properties.recoil
    }

    private fun applyRecoil(pos: Vector3, recoil: Float) {
        if (recoil == 0f) {
            return
        }
        val deviation = nextDouble(recoil * -1.0, recoil.toDouble())
        pos.x += deviation.toFloat() * 0.5f
        pos.z += deviation.toFloat()
    }

    private fun calculateCoolDown(properties: RangedWeaponProperties): Float {
        val roundsPerMinute = properties.fireRate
        val roundsPerSecond = roundsPerMinute / 60f
        return 1 / roundsPerSecond
    }
}
