package bke.iso.game.weapon

import bke.iso.engine.collision.Collider
import bke.iso.engine.math.toScreen
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.atan2
import kotlin.math.min
import kotlin.random.Random.Default.nextFloat

const val MAX_RECOIL = 2f

interface WeaponLogic

class RangedWeaponLogic(private val world: World) : WeaponLogic {

    private val log = KotlinLogging.logger {}

    fun shoot(
        shooter: Actor,
        target: Vector3,
        weapon: RangedWeaponItem,
        properties: RangedWeaponProperties
    ) {
        if (weapon.ammo <= 0 || weapon.coolDown > 0f || weapon.reloadCoolDown > 0f) {
            return
        }

        applySpread(target, properties)
        applyRecoil(target, weapon)
        createBullet(shooter, target, properties)
        weapon.ammo--
        weapon.coolDown = calculateCoolDown(properties)
        weapon.recoil = min(weapon.recoil + properties.recoil, MAX_RECOIL)

        if (weapon.ammo <= 0) {
            reload(weapon, properties)
        }
    }

    fun reload(weapon: RangedWeaponItem, properties: RangedWeaponProperties) {
        if (weapon.reloadCoolDown > 0f) {
            return
        }
        log.debug { "reloading" }
        weapon.reloadCoolDown = properties.reloadTime
    }

    private fun createBullet(
        shooter: Actor,
        target: Vector3,
        properties: RangedWeaponProperties
    ) {
        val start = shooter.pos
        shooter.with { offset: RangedWeaponOffset ->
            start.add(offset.x, offset.y, offset.z)
        }

        val velocity = Vector3(target)
            .sub(start)
            .nor()
            .scl(properties.velocity)

        world.actors.create(
            start,
            Bullet(shooter.id, properties.damage, start),
            Sprite(
                "bullet2.png",
                offsetX = 16f,
                offsetY = 16f,
                scale = 1.2f,
                rotation = calculateBulletRotationDegrees(start, target)
            ),
            PhysicsBody(PhysicsMode.GHOST, velocity),
            Collider(
                size = Vector3(0.125f, 0.125f, 0.125f),
                offset = Vector3(0f, -0.125f, 0f)
            ),
            DebugSettings().apply {
                zAxis = false
            }
        )
    }

    private fun calculateBulletRotationDegrees(start: Vector3, target: Vector3): Float {
        val difference = toScreen(target).sub(toScreen(start))
        val angleRadians = atan2(difference.y, difference.x)
        return angleRadians * MathUtils.radiansToDegrees
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
