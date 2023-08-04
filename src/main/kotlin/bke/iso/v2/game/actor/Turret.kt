package bke.iso.v2.game.actor

import bke.iso.v2.engine.System
import bke.iso.v2.engine.physics.Collisions
import bke.iso.v2.engine.render.DebugRenderer
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.Component
import bke.iso.v2.engine.world.World
import bke.iso.v2.game.Combat
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.collision.Segment

data class Turret(
    var elapsedCooldownTime: Float = 0f,
    var canShoot: Boolean = true
) : Component()

private const val VISION_RADIUS = 12f
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

            debugRenderer.addSphere(actor.pos, VISION_RADIUS, Color.GOLD)
            engagePlayer(actor, turret)
        }
    }

    private fun engagePlayer(turretObject: Actor, turret: Turret) {
        val (playerObject, _) = world.findActorWith<Player>() ?: return
        if (canSee(turretObject, playerObject) && turret.canShoot) {
            combat.shoot(turretObject, playerObject.pos, BulletType.TURRET)
            turret.elapsedCooldownTime = 0f
            turret.canShoot = false
        }
    }

    private fun canSee(actor: Actor, other: Actor): Boolean {
        val start = actor.pos
        val end = other.pos
        if (start.dst(end) > VISION_RADIUS) {
            return false
        }

        val firstCollision = collisions
            .checkCollisions(Segment(start, end))
            .filter { collision -> collision.obj is Actor && collision.obj != actor }
            .minByOrNull { collision -> collision.distanceStart }
            ?: return false

        val firstPoint = firstCollision
            .points
            .minBy { point -> start.dst(point) }
        debugRenderer.addPoint(firstPoint, 3f, Color.RED)

        return if (firstCollision.obj == other) {
            debugRenderer.addLine(start, end, 1f, Color.RED)
            true
        } else {
            debugRenderer.addLine(start, firstPoint, 1f, Color.RED)
            false
        }
    }
}
