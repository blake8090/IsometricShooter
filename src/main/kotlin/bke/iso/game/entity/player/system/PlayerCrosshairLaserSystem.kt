package bke.iso.game.entity.player.system

import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.SegmentCollision
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.state.System
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.game.entity.player.Player
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.applyRangedWeaponOffset
import bke.iso.game.weapon.system.Bullet
import bke.iso.game.weapon.system.Explosion
import bke.iso.game.weapon.system.RangedWeapon
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
    private val collisions: Collisions,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.entities.each<Player> { actor, _ ->
            update(actor)
        }
    }

    private fun update(playerEntity: Entity) {
        if (!renderer.pointer.visible || weaponsModule.getSelectedWeapon(playerEntity) !is RangedWeapon) {
            return
        }

        val start = playerEntity.pos
        applyRangedWeaponOffset(playerEntity, start)

        val mid = toWorld(renderer.pointer.pos, playerEntity.z)
        applyRangedWeaponOffset(playerEntity, mid)

        val end = extend(start, mid, 3f)

        val firstCollision = collisions
            .checkLineCollisions(start, end)
            .filter { collision -> filterCollision(playerEntity, collision) }
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

    private fun filterCollision(playerEntity: Entity, collision: SegmentCollision): Boolean =
        collision.entity != playerEntity
                && !collision.entity.has<Bullet>()
                && !collision.entity.has<Explosion>()

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
