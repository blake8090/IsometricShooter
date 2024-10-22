package bke.iso.game.actor.player.system

import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.SegmentCollision
import bke.iso.engine.render.Renderer
import bke.iso.engine.state.System
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.actor.player.Player
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.system.Bullet
import bke.iso.game.weapon.system.Explosion
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import kotlin.math.max

/**
 * Handles drawing a laser pointer from the player's crosshair.
 */
class PlayerCrosshairLaserSystem(
    private val world: World,
    private val renderer: Renderer,
    private val weaponsModule: WeaponsModule,
    private val collisions: Collisions
) : System {

    override fun update(deltaTime: Float) {
        world.actors.each<Player> { actor, _ ->
            update(actor)
        }
    }

    private fun update(playerActor: Actor) {
        val start = weaponsModule.getShootPos(playerActor)

        val mid = renderer.pointer.worldPos
        mid.z = start.z

        val end = extend(start, mid, 3f)

        val firstCollision = collisions
            .checkLineCollisions(start, end)
            .filter { collision -> filterCollision(playerActor, collision) }
            .minByOrNull { collision -> collision.distanceStart }

        if (firstCollision == null) {
            renderer.fgShapes.addLine(start, end, 1f, Color.GREEN)
        } else {
            val firstPoint = firstCollision.points.minByOrNull { point -> start.dst(point) }

            checkNotNull(firstPoint) {
                "Expected at least one point for collision $firstCollision"
            }

            renderer.fgShapes.addLine(start, firstPoint, 1f, Color.GREEN)
            renderer.fgShapes.addLine(firstPoint, end, 1f, Color.RED)
            renderer.fgShapes.addPoint(firstPoint, 2f, Color.GREEN)
        }
    }

    private fun filterCollision(playerActor: Actor, collision: SegmentCollision): Boolean =
        collision.obj is Actor
                && collision.obj != playerActor
                && !collision.obj.has<Bullet>()
                && !collision.obj.has<Explosion>()

    private fun extend(start: Vector3, end: Vector3, distance: Float): Vector3 {
        val direction = Vector3(end)
            .sub(start)
            .nor()
        val ray = Ray(end, direction)

        val result = Vector3()
        // TODO: add helper that auto provides the result
        ray.getEndPoint(result, max(16f, distance))

        return result
    }
}
