package bke.iso.game.actor

import bke.iso.engine.Game
import bke.iso.engine.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.DebugRenderer
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.World
import bke.iso.game.player.Player
import bke.iso.game.weapon.Inventory
import bke.iso.game.weapon.RangedWeapon
import bke.iso.game.weapon.RangedWeaponOffset
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("turret")
class Turret : Component

private const val RANGE_RADIUS = 12f
private const val GUN_HEIGHT = 0.7f

class TurretSystem(
    private val world: World,
    private val collisions: Collisions,
    private val debugRenderer: DebugRenderer,
    private val events: Game.Events,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.actors.each<Turret> { actor, _ ->
            update(actor)
        }
    }

    private fun update(turretActor: Actor) {
        if (!turretActor.has<Inventory>()) {
            weaponsModule.equip(turretActor, "turret")
            turretActor.add(RangedWeaponOffset(0f, 0f, GUN_HEIGHT))
        }

        debugRenderer.addSphere(turretActor.pos, RANGE_RADIUS, Color.GOLD)

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
        start.z += GUN_HEIGHT
        val end = getTargetPos(target)

        val firstCollision = collisions
            .checkCollisions(Segment(start, end))
            .filter { collision -> collision.obj is Actor && collision.obj != actor }
            .minByOrNull { collision -> collision.distanceStart }
            ?: return false

        val firstPoint = firstCollision
            .points
            .minBy { point -> start.dst(point) }
        debugRenderer.addPoint(firstPoint, 3f, Color.RED)

        return if (firstCollision.obj == target) {
            debugRenderer.addLine(start, end, 1f, Color.RED)
            true
        } else {
            debugRenderer.addLine(start, firstPoint, 1f, Color.RED)
            false
        }
    }

    private fun getTargetPos(target: Actor): Vector3 {
        val pos = target.pos

        // aim for center mass!
        target.get<Collider>()?.let { collider ->
            pos.z += collider.size.z * 0.5f
        }

        return pos
    }
}
