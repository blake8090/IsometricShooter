package bke.iso.game.actor

import bke.iso.engine.core.Events
import bke.iso.engine.state.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.World
import bke.iso.game.actor.player.Player
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
        world.actors.each<Turret> { actor, _ ->
            update(actor)
        }
    }

    private fun update(turretActor: Actor) {
        debugRenderer.category("turret").addSphere(turretActor.pos, RANGE_RADIUS, Color.GOLD)

        val playerActor = world.actors.find<Player>() ?: return
        if (withinRange(turretActor, playerActor) && canSee(turretActor, playerActor)) {
            events.fire(WeaponsModule.ShootEvent(turretActor, getTargetPos(playerActor)))
        }

        val weapon = weaponsModule.getSelectedWeapon(turretActor)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(turretActor))
        }
    }

    private fun withinRange(actor: Actor, target: Actor) =
        actor.pos.dst(target.pos) <= RANGE_RADIUS

    private fun canSee(actor: Actor, target: Actor): Boolean {
        val start = actor.pos
        actor.with<RangedWeaponOffset> { offset ->
            start.z += offset.z
        }
        val end = getTargetPos(target)

        val firstCollision = collisions
            .checkLineCollisions(start, end)
            .filter { collision -> collision.actor != actor }
            .minByOrNull { collision -> collision.distanceStart }
            ?: return false

        val firstPoint = firstCollision
            .points
            .minBy { point -> start.dst(point) }
        debugRenderer.category("vision").addPoint(firstPoint, 3f, Color.RED)

        return if (firstCollision.actor == target) {
            debugRenderer.category("vision").addLine(start, end, 1f, Color.RED)
            true
        } else {
            debugRenderer.category("vision").addLine(start, firstPoint, 1f, Color.RED)
            false
        }
    }

    private fun getTargetPos(target: Actor): Vector3 {
        val pos = target.pos

        // aim for center mass!
        target.with<Collider> { collider ->
            pos.z += collider.size.z * 0.5f
        }

        return pos
    }
}
