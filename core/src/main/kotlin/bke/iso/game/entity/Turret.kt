package bke.iso.game.entity

import bke.iso.engine.core.Events
import bke.iso.engine.state.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.World
import bke.iso.game.entity.player.Player
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.system.RangedWeaponOffset
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("turret")
class Turret : Component

private const val RANGE_RADIUS = 12f

class TurretSystem(
    private val world: World,
    private val collisions: Collisions,
    private val debugRenderer: DebugRenderer,
    private val events: Events,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.entities.each<Turret> { entity, _ ->
            update(entity)
        }
    }

    private fun update(turretEntity: Entity) {
        debugRenderer.category("turret").addSphere(turretEntity.pos, RANGE_RADIUS, Color.GOLD)

        val playerEntity = world.entities.find<Player>() ?: return
        if (withinRange(turretEntity, playerEntity) && canSee(turretEntity, playerEntity)) {
            events.fire(WeaponsModule.ShootEvent(turretEntity, getTargetPos(playerEntity)))
        }

        val weapon = weaponsModule.getSelectedWeapon(turretEntity)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(turretEntity))
        }
    }

    private fun withinRange(entity: Entity, target: Entity) =
        entity.pos.dst(target.pos) <= RANGE_RADIUS

    private fun canSee(entity: Entity, target: Entity): Boolean {
        val start = entity.pos
        entity.with<RangedWeaponOffset> { offset ->
            start.z += offset.z
        }
        val end = getTargetPos(target)

        val firstCollision = collisions
            .checkLineCollisions(start, end)
            .filter { collision -> collision.entity != entity }
            .minByOrNull { collision -> collision.distanceStart }
            ?: return false

        val firstPoint = firstCollision
            .points
            .minBy { point -> start.dst(point) }
        debugRenderer.category("vision").addPoint(firstPoint, 3f, Color.RED)

        return if (firstCollision.entity == target) {
            debugRenderer.category("vision").addLine(start, end, 1f, Color.RED)
            true
        } else {
            debugRenderer.category("vision").addLine(start, firstPoint, 1f, Color.RED)
            false
        }
    }

    private fun getTargetPos(target: Entity): Vector3 {
        val pos = target.pos

        // aim for center mass!
        target.with<Collider> { collider ->
            pos.z += collider.size.z * 0.5f
        }

        return pos
    }
}
