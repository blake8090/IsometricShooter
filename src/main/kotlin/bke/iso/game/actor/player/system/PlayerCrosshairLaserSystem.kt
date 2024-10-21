package bke.iso.game.actor.player.system

import bke.iso.engine.Events
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.SegmentCollision
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import bke.iso.engine.state.System
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.has
import bke.iso.game.actor.player.Player
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.system.Bullet
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
        val end = renderer.pointer.worldPos
        end.z = start.z

//        renderer.fgShapes.addLine(
//            start,
//            end,
//            1f,
//            Color.RED
//        )
//
//        renderer.fgShapes.addLine(
//            end,
//            Vector3(end.x, end.y, 0f),
//            1f,
//            Color.SCARLET
//        )
//        val pickStart = Vector3(start)
//        val pickEnd = extend(start, end, 15f)
//
//        renderer.fgShapes.addLine(
//            start,
//            pickEnd,
//            1f,
//            Color.RED
//        )
//
//        renderer.fgShapes.addLine(
//            end,
//            Vector3(end.x, end.y, 0f),
//            1f,
//            Color.SCARLET
//        )

//        val collisions = collisions
//            .checkLineCollisions(start, pickEnd)
//            .filter { collision -> collision.obj is Actor && collision.obj != playerActor }
//
//        for (collision in collisions) {
//            for (point in collision.points) {
//                renderer.fgShapes.addPoint(point, 3f, Color.GREEN)
//            }
//        }

//        for (collision in collisions.checkLineCollisions(end, pickEnd)) {
//            for (point in collision.points) {
//                renderer.fgShapes.addPoint(point, 3f, Color.GREEN)
//            }
//        }

    }

    private fun extend(start: Vector3, end: Vector3, distance: Float): Vector3 {
        val direction = Vector3(end)
            .sub(start)
            .nor()
        val ray = Ray(end, direction)

        val result = Vector3()
        // TODO: add helper that auto provides the result
        ray.getEndPoint(result, max(15f, distance))

        renderer.fgShapes.addLine(
            end,
            result,
            1f,
            Color.GREEN
        )

        renderer.fgShapes.addPoint(result, 5f, Color.CYAN)
        return result
    }
}
