package bke.iso.game.weapon

import bke.iso.engine.core.Event
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Module
import bke.iso.engine.math.nextFloat
import bke.iso.engine.math.toScreen
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import bke.iso.game.entity.Inventory
import bke.iso.game.weapon.system.Bullet
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.system.RangedWeaponOffset
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.atan2
import kotlin.math.min

private const val MAX_RECOIL = 2f

class WeaponsModule(
    private val assets: Assets,
    private val world: World
) : Module {

    private val log = KotlinLogging.logger {}

    override val alwaysActive: Boolean = false

    override fun handleEvent(event: Event) {
        if (event is ShootEvent) {
            shoot(event.entity, event.target)
        } else if (event is ReloadEvent) {
            reload(event.entity)
        }
    }

    fun equip(entity: Entity, name: String) {
        val properties = assets.get<WeaponProperties>(name)

        if (properties is RangedWeaponProperties) {
            val inventory = entity.getOrAdd(Inventory())
            inventory.selectedWeapon = RangedWeapon(name, properties.magSize)
            log.debug { "Actor $entity equipped weapon '$name'" }
        }
    }

    private fun shoot(entity: Entity, target: Vector3) {
        val weapon = getSelectedWeapon(entity)
        if (weapon !is RangedWeapon || !canShoot(weapon)) {
            return
        }

        val properties = getProperties(weapon)
        applySpread(target, properties)
        applyRecoil(target, weapon)
        createBullet(entity, target, properties)

        weapon.ammo--
        weapon.coolDown = calculateCoolDown(properties)
        weapon.recoil = min(weapon.recoil + properties.recoil, MAX_RECOIL)
    }

    private fun canShoot(weapon: RangedWeapon) =
        weapon.ammo > 0f && weapon.coolDown == 0f && weapon.reloadCoolDown == 0f


    private fun reload(entity: Entity) {
        val weapon = getSelectedWeapon(entity) ?: return
        if (weapon !is RangedWeapon || weapon.reloadCoolDown > 0f) {
            return
        }

        log.debug { "Actor '$entity' is reloading weapon '${weapon.name}'" }
        val properties = getProperties(weapon)
        weapon.reloadCoolDown = properties.reloadTime
    }

    private fun calculateCoolDown(properties: RangedWeaponProperties): Float {
        val roundsPerMinute = properties.fireRate
        val roundsPerSecond = roundsPerMinute / 60f
        return 1 / roundsPerSecond
    }

    fun getSelectedWeapon(entity: Entity) =
        entity.get<Inventory>()
            ?.selectedWeapon

    fun getProperties(weapon: RangedWeapon): RangedWeaponProperties {
        val properties = assets.get<WeaponProperties>(weapon.name)
        check(properties is RangedWeaponProperties) {
            "Weapon '${weapon.name}' is not a ranged weapon"
        }
        return properties
    }

    private fun createBullet(shooter: Entity, target: Vector3, properties: RangedWeaponProperties) {
        // TODO: should the shootPos be included on the event object instead?
        val start = shooter.pos
        applyRangedWeaponOffset(shooter, start)

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

        world.entities.create(
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
        val entity: Entity,
        val target: Vector3
    ) : Event

    data class ReloadEvent(val entity: Entity) : Event
}

fun applyRangedWeaponOffset(entity: Entity, pos: Vector3) {
    entity.with { offset: RangedWeaponOffset ->
        pos.add(offset.x, offset.y, offset.z)
    }
}
