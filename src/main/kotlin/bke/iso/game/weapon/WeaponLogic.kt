package bke.iso.game.weapon

import bke.iso.engine.collision.Collider
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import kotlin.math.min
import kotlin.random.Random.Default.nextFloat

const val MAX_RECOIL = 2f

interface WeaponLogic

class RangedWeaponLogic(private val world: World) : WeaponLogic {

    fun shoot(
        shooter: Actor,
        target: Vector3,
        weapon: RangedWeaponItem,
        properties: RangedWeaponProperties
    ) {
        if (weapon.ammo <= 0 || weapon.coolDown > 0f) {
            return
        }

        applySpread(target, properties)
        applyRecoil(target, weapon)

        val start = shooter.pos.add(weapon.offset)
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

        weapon.ammo--
        weapon.coolDown = calculateCoolDown(properties)
        weapon.recoil = min(weapon.recoil + properties.recoil, MAX_RECOIL)
    }

    private fun applySpread(target: Vector3, properties: RangedWeaponProperties) {
        val deviation = properties.spread * nextFloat(-1.0f, 1.0f)
        target.add(deviation)
    }

    private fun applyRecoil(target: Vector3, weapon: RangedWeaponItem) {
        target.x += weapon.recoil * nextFloat(-1.0f, 1.0f) * 0.25f
        target.y += weapon.recoil * nextFloat(-1.0f, 1.0f) * 0.25f
        target.z += weapon.recoil * nextFloat(-1.0f, 1.0f)
    }

    private fun calculateCoolDown(properties: RangedWeaponProperties): Float {
        val roundsPerMinute = properties.fireRate
        val roundsPerSecond = roundsPerMinute / 60f
        return 1 / roundsPerSecond
    }

    private fun nextFloat(min: Float, max: Float) =
        nextFloat() * (max - min) + min
}
