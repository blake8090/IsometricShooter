package bke.iso.game.weapon

import bke.iso.engine.Event
import bke.iso.engine.state.Module
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.nextFloat
import bke.iso.engine.math.toScreen
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import bke.iso.game.actor.Inventory
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.atan2
import kotlin.math.min

private const val MAX_RECOIL = 2f

class WeaponsModule(
    private val assets: Assets,
    private val world: World
) : Module {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
    }

    override fun handleEvent(event: Event) {
        if (event is ShootEvent) {
            shoot(event.actor, event.target)
        } else if (event is ReloadEvent) {
            reload(event.actor)
        }
    }

    fun equip(actor: Actor, name: String) {
        val properties = assets.get<WeaponProperties>(name)

        if (properties is RangedWeaponProperties) {
            val inventory = actor.getOrAdd(Inventory())
            inventory.selectedWeapon = RangedWeapon(name, properties.magSize)
            log.debug { "Actor $actor equipped weapon '$name'" }
        }
    }

    private fun shoot(actor: Actor, target: Vector3) {
        val weapon = getSelectedWeapon(actor)
        if (weapon !is RangedWeapon || !canShoot(weapon)) {
            return
        }

        val properties = getProperties(weapon)
        applySpread(target, properties)
        applyRecoil(target, weapon)
        createBullet(actor, target, properties)

        weapon.ammo--
        weapon.coolDown = calculateCoolDown(properties)
        weapon.recoil = min(weapon.recoil + properties.recoil, MAX_RECOIL)
    }

    private fun canShoot(weapon: RangedWeapon) =
        weapon.ammo > 0f && weapon.coolDown == 0f && weapon.reloadCoolDown == 0f


    private fun reload(actor: Actor) {
        val weapon = getSelectedWeapon(actor) ?: return
        if (weapon !is RangedWeapon || weapon.reloadCoolDown > 0f) {
            return
        }

        log.debug { "Actor '$actor' is reloading weapon '${weapon.name}'" }
        val properties = getProperties(weapon)
        weapon.reloadCoolDown = properties.reloadTime
    }

    private fun calculateCoolDown(properties: RangedWeaponProperties): Float {
        val roundsPerMinute = properties.fireRate
        val roundsPerSecond = roundsPerMinute / 60f
        return 1 / roundsPerSecond
    }

    fun getSelectedWeapon(actor: Actor) =
        actor.get<Inventory>()
            ?.selectedWeapon

    fun getProperties(weapon: RangedWeapon): RangedWeaponProperties {
        val properties = assets.get<WeaponProperties>(weapon.name)
        check(properties is RangedWeaponProperties) {
            "Weapon '${weapon.name}' is not a ranged weapon"
        }
        return properties
    }

    private fun createBullet(shooter: Actor, target: Vector3, properties: RangedWeaponProperties) {
        val start = getShootPos(shooter)

        val velocity = Vector3(target)
            .sub(start)
            .nor()
            .scl(properties.velocity)

        val rotation =
            if (properties.bulletRotation) {
                calculateBulletRotationDegrees(start, target)
            } else {
                0f
            }

        world.actors.create(
            start,
            Bullet(shooter.id, properties.damage, properties.range, start),
            Sprite(
                properties.bulletTexture,
                offsetX = 16f,
                offsetY = 16f,
                scale = 1f,
                rotation = rotation
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

    fun getShootPos(actor: Actor): Vector3 {
        val start = actor.pos

        actor.with { offset: RangedWeaponOffset ->
            start.add(offset.x, offset.y, offset.z)
        }

        return start
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

    private fun applyRecoil(target: Vector3, weapon: RangedWeapon) {
        target.x += weapon.recoil * nextFloat(-1.0f, 1.0f) * 0.25f
        target.y += weapon.recoil * nextFloat(-1.0f, 1.0f) * 0.25f
        target.z += weapon.recoil * nextFloat(-1.0f, 1.0f)
    }

    data class ShootEvent(
        val actor: Actor,
        val target: Vector3
    ) : Event

    data class ReloadEvent(val actor: Actor) : Event
}
