package bke.iso.engine.physics

import bke.iso.engine.math.Box
import bke.iso.engine.math.getRay
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.world.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.collision.Segment
import mu.KotlinLogging
import kotlin.math.ceil
import kotlin.math.floor

class Collisions(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        game.world.actorsWith<FrameCollisions> { actor, _ ->
            actor.components.remove<FrameCollisions>()
        }
    }

    fun checkCollisions(segment: Segment): Set<SegmentCollision> {
        val area = Box(segment.a, segment.b)
        game.renderer.debugRenderer.addBox(area, Color.ORANGE)

        val ray = segment.getRay()
        val collisions = mutableSetOf<SegmentCollision>()
        for (gameObject in game.world.getObjectsInArea(area)) {
            val data = gameObject.getCollisionData() ?: continue
            val points = mutableSetOf<Vector3>()
            for (face in data.box.faces) {
                findIntersection(ray, face)
                    ?.let(points::add)
            }

            if (points.isNotEmpty()) {
                val center = data.box.center
                collisions.add(
                    SegmentCollision(
                        gameObject,
                        data,
                        segment.a.dst(center),
                        segment.b.dst(center),
                        points
                    )
                )
            }
        }

        return collisions
    }

    private fun findIntersection(ray: Ray, box: BoundingBox): Vector3? {
        val point = Vector3()
        return if (Intersector.intersectRayBounds(ray, box, point)) {
            point
        } else {
            null
        }
    }

    fun predictCollisions(actor: Actor, dx: Float, dy: Float, dz: Float): Set<PredictedCollision> {
        val data = actor.getCollisionData() ?: return emptySet()
        val box = data.box

        // broad-phase: instead of iterating through every object, only check entities within general area of movement
        val px = if (dx < 0) floor(dx) else ceil(dx)
        val py = if (dy < 0) floor(dy) else ceil(dy)
        val pz = if (dz < 0) floor(dz) else ceil(dz)
        val projectedBox = box.project(px, py, pz)
        game.renderer.debugRenderer.addBox(projectedBox, Color.ORANGE)

        val objects = game.world.getObjectsInArea(projectedBox)

        // narrow-phase: check precise collisions for each object within area
        val collisions = mutableSetOf<PredictedCollision>()
        for (other in objects) {
            if (actor == other) {
                continue
            }

            val otherData = other.getCollisionData() ?: continue
            checkSweptCollision(box, Vector3(dx, dy, dz), otherData.box)?.let { collision ->
                val distance = box.center.dst(otherData.box.center)
                val side = getCollisionSide(collision.hitNormal)
                log.trace("dist: $distance, collision time: ${collision.collisionTime}, hit normal: ${collision.hitNormal}, side: $side")

                collisions.add(
                    PredictedCollision(
                        other,
                        otherData,
                        distance,
                        collision.collisionTime,
                        collision.hitNormal,
                        side
                    )
                )
            }
        }

        recordCollisions(actor, collisions)
        return collisions
    }

    private fun recordCollisions(actor: Actor, predictedCollisions: Collection<PredictedCollision>) {
        val frameCollisions = actor.components.getOrPut(FrameCollisions())
        predictedCollisions
            .filter { collision ->
                frameCollisions.collisions.none { collision.obj is Actor && collision.obj == actor }
            }
            .map { Collision(it.obj, it.data, it.distance, it.side) }
            .forEach(frameCollisions.collisions::add)
    }

    private data class SweptCollision(
        val collisionTime: Float,
        val hitNormal: Vector3
    )

    private fun checkSweptCollision(a: Box, delta: Vector3, b: Box): SweptCollision? {
        // if box B is not in the projection of box A, both boxes will never collide
        val pBox = a.project(delta.x, delta.y, delta.z)
        if (!pBox.intersects(b)) {
            return null
        }

        /**
         * Represents the distance between the nearest sides of boxes A and B.
         * Box A would need to travel this distance to begin overlapping box B.
         */
        val entryDistanceX =
            if (delta.x > 0f) {
                b.min.x - a.max.x
            } else {
                b.max.x - a.min.x
            }

        /**
         * Represents the time it will take for box A to begin overlapping box B.
         * Calculated using (distance / speed).
         */
        val entryTimeX =
            if (delta.x != 0f) {
                entryDistanceX / delta.x
            } else {
                Float.NEGATIVE_INFINITY
            }

        /**
         * Represents the distance between the farthest sides of boxes A and B.
         * Box A would need to travel this distance to stop overlapping box B.
         */
        val exitDistanceX =
            if (delta.x > 0f) {
                b.max.x - a.min.x
            } else {
                b.min.x - a.max.x
            }

        /**
         * Represents the time it will take for box A to stop overlapping box B.
         * Calculated using (distance / speed).
         */
        val exitTimeX =
            if (delta.x != 0f) {
                exitDistanceX / delta.x
            } else {
                Float.POSITIVE_INFINITY
            }

        // y-axis
        val entryDistanceY =
            if (delta.y > 0f) {
                b.min.y - a.max.y
            } else {
                b.max.y - a.min.y
            }

        val entryTimeY =
            if (delta.y != 0f) {
                entryDistanceY / delta.y
            } else {
                Float.NEGATIVE_INFINITY
            }

        val exitDistanceY =
            if (delta.y > 0f) {
                b.max.y - a.min.y
            } else {
                b.min.y - a.max.y
            }

        val exitTimeY =
            if (delta.y != 0f) {
                exitDistanceY / delta.y
            } else {
                Float.POSITIVE_INFINITY
            }

        // z-axis
        val entryDistanceZ =
            if (delta.z > 0f) {
                b.min.z - a.max.z
            } else {
                b.max.z - a.min.z
            }

        val entryTimeZ =
            if (delta.z != 0f) {
                entryDistanceZ / delta.z
            } else {
                Float.NEGATIVE_INFINITY
            }

        val exitDistanceZ =
            if (delta.z > 0f) {
                b.max.z - a.min.z
            } else {
                b.min.z - a.max.z
            }

        val exitTimeZ =
            if (delta.z != 0f) {
                exitDistanceZ / delta.z
            } else {
                Float.POSITIVE_INFINITY
            }

        // if the time ranges on both axes never overlap, there's no collision
        if ((entryTimeX > exitTimeY && entryTimeX > exitTimeZ) ||
            (entryTimeY > exitTimeX && entryTimeY > exitTimeZ) ||
            (entryTimeZ > exitTimeX && entryTimeZ > exitTimeY)
        ) {
            return null
        }

        // find the longest entry time. the shortest entry time only demonstrates a collision on one axis.
        val entryTime = maxOf(entryTimeX, entryTimeY, entryTimeZ)

        // if the entryTime is not within the 0-1 range, that means no collision occurred
        if (entryTime !in 0f..1f) {
            return null
        }

        var hitNormalX = 0f
        var hitNormalY = 0f
        var hitNormalZ = 0f
        if (entryTimeX > entryTimeY && entryTimeX > entryTimeZ) {
            hitNormalX = if (delta.x > 0) -1f else 1f
        } else if (entryTimeY > entryTimeX && entryTimeY > entryTimeZ) {
            hitNormalY = if (delta.y > 0) -1f else 1f
        } else {
            hitNormalZ = if (delta.z > 0) -1f else 1f
        }

        /**
         * The hit normal points either up, down, left, right, top, or bottom.
         * Box B would need to push box A in this direction to stop box A from moving.
         * Using this, the collision direction can be determined.
         */
        val hitNormal = Vector3(hitNormalX, hitNormalY, hitNormalZ)
        return SweptCollision(entryTime, hitNormal)
    }

    private fun getCollisionSide(hitNormal: Vector3): BoxCollisionSide =
        when (hitNormal) {
            Vector3(-1f, 0f, 0f) -> BoxCollisionSide.LEFT
            Vector3(1f, 0f, 0f) -> BoxCollisionSide.RIGHT
            Vector3(0f, -1f, 0f) -> BoxCollisionSide.FRONT
            Vector3(0f, 1f, 0f) -> BoxCollisionSide.BACK
            Vector3(0f, 0f, -1f) -> BoxCollisionSide.BOTTOM
            Vector3(0f, 0f, 1f) -> BoxCollisionSide.TOP
            else -> BoxCollisionSide.CORNER
        }
}
