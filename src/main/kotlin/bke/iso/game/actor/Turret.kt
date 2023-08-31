package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.World
import bke.iso.game.Combat
import bke.iso.game.player.Player
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.collision.Segment

data class Turret(
    var elapsedCooldownTime: Float = 0f,
    var canShoot: Boolean = true
) : Component()

private const val RANGE_RADIUS = 12f
private const val MINIMUM_COOLDOWN_SECONDS = 0.8f

class TurretSystem(
    private val world: World,
    private val collisions: Collisions,
    private val debugRenderer: DebugRenderer,
    private val combat: Combat
) : System {

    override fun update(deltaTime: Float) {
        world.actorsWith<Turret> { actor, turret ->
            turret.elapsedCooldownTime += deltaTime
            if (turret.elapsedCooldownTime >= MINIMUM_COOLDOWN_SECONDS) {
                turret.elapsedCooldownTime = 0f
                turret.canShoot = true
            }

            debugRenderer.addSphere(actor.pos, RANGE_RADIUS, Color.GOLD)
            engagePlayer(actor, turret)
        }
    }

    private fun engagePlayer(turretActor: Actor, turret: Turret) {
        val (player, _) = world.findActorWith<Player>() ?: return
        if (withinRange(turretActor, player) && canSee(turretActor, player)) {
            shoot(turretActor, turret, player)
        }
    }

    private fun shoot(turretActor: Actor, turret: Turret, target: Actor) {
        if (!turret.canShoot) {
            return
        }
        combat.shoot(turretActor, target.pos, BulletType.TURRET)
        turret.elapsedCooldownTime = 0f
        turret.canShoot = false
    }

    private fun withinRange(actor: Actor, target: Actor) =
        actor.pos.dst(target.pos) <= RANGE_RADIUS

    private fun canSee(actor: Actor, target: Actor): Boolean {
        val start = actor.pos
        val end = target.pos

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
}
