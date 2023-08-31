package bke.iso.engine.collision

import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3

data class SweptCollision(
    /**
     * The normalized collision time between 0 and 1.
     * 0 represents the start of the frame, and 1 represents the end.
     */
    val collisionTime: Float,
    /**
     * The hit normal points either up, down, left, right, top, or bottom.
     * Box B would need to push box A in this direction to stop box A from moving.
     * Using this, the collision direction can be determined.
     *
     * For example, a hit normal of `(-1, 0, 0)` means that box A collided with the left side of box B.
     */
    val hitNormal: Vector3
)

/**
 * Given an axis-aligned bounding box and its movement delta, checks for a collision with another box.
 * @param a The first box
 * @param b The second box
 * @param delta The first box's movement delta
 * @return A [SweptCollision] if a collision occurred, otherwise null
 */
fun sweepTest(a: Box, b: Box, delta: Vector3): SweptCollision? {
    // if box B is not in the projected movement of box A, both boxes will never collide
    val pBox = a.expand(delta.x, delta.y, delta.z)
    if (!pBox.intersects(b)) {
        return null
    }

    /**
     * Represents the distance between the closest sides of boxes A and B.
     * Box A would need to travel this distance to begin overlapping box B.
     */
    val entryDistance = getEntryDistance(a, b, delta)

    /**
     * Represents the time it will take for box A to begin overlapping box B.
     * Calculated using (distance / speed).
     */
    val entryTime = getEntryTime(entryDistance, delta)

    /**
     * Represents the distance between the farthest sides of boxes A and B.
     * Box A would need to travel this distance to stop overlapping box B.
     */
    val exitDistance = getExitDistance(a, b, delta)

    /**
     * Represents the time it will take for box A to stop overlapping box B.
     * Calculated using (distance / speed).
     */
    val exitTime = getExitTime(exitDistance, delta)

    return findSweptCollision(entryTime, exitTime, delta)
}

private fun findSweptCollision(entryTime: Vector3, exitTime: Vector3, delta: Vector3): SweptCollision? {
    // if the time ranges on all axes never overlap, there's no collision
    val noOverlapX = entryTime.x > exitTime.y && entryTime.x > exitTime.z
    val noOverlapY = entryTime.y > exitTime.x && entryTime.y > exitTime.z
    val noOverlapZ = entryTime.z > exitTime.x && entryTime.z > exitTime.y
    if (noOverlapX || noOverlapY || noOverlapZ) {
        return null
    }

    // find the longest entry time. the shortest entry time only demonstrates a collision on one axis.
    val longestEntryTime = maxOf(entryTime.x, entryTime.y, entryTime.z)
    // if the longest entry time is not within the 0-1 range, that means no collision occurred
    return if (longestEntryTime !in 0f..1f) {
        null
    } else {
        SweptCollision(longestEntryTime, getHitNormal(entryTime, delta))
    }
}

private fun getHitNormal(entryTime: Vector3, delta: Vector3): Vector3 {
    val hitNormal = Vector3()
    if (entryTime.x > entryTime.y && entryTime.x > entryTime.z) {
        hitNormal.x = if (delta.x > 0) -1f else 1f
    } else if (entryTime.y > entryTime.x && entryTime.y > entryTime.z) {
        hitNormal.y = if (delta.y > 0) -1f else 1f
    } else {
        hitNormal.z = if (delta.z > 0) -1f else 1f
    }
    return hitNormal
}

private fun getEntryDistance(a: Box, b: Box, delta: Vector3) =
    Vector3(
        if (delta.x > 0f) {
            b.min.x - a.max.x
        } else {
            b.max.x - a.min.x
        },

        if (delta.y > 0f) {
            b.min.y - a.max.y
        } else {
            b.max.y - a.min.y
        },

        if (delta.z > 0f) {
            b.min.z - a.max.z
        } else {
            b.max.z - a.min.z
        }
    )

private fun getEntryTime(entryDistance: Vector3, delta: Vector3) =
    Vector3(
        if (delta.x != 0f) {
            entryDistance.x / delta.x
        } else {
            Float.NEGATIVE_INFINITY
        },

        if (delta.y != 0f) {
            entryDistance.y / delta.y
        } else {
            Float.NEGATIVE_INFINITY
        },

        if (delta.z != 0f) {
            entryDistance.z / delta.z
        } else {
            Float.NEGATIVE_INFINITY
        }
    )

private fun getExitDistance(a: Box, b: Box, delta: Vector3) =
    Vector3(
        if (delta.x > 0f) {
            b.max.x - a.min.x
        } else {
            b.min.x - a.max.x
        },

        if (delta.y > 0f) {
            b.max.y - a.min.y
        } else {
            b.min.y - a.max.y
        },

        if (delta.z > 0f) {
            b.max.z - a.min.z
        } else {
            b.min.z - a.max.z
        }
    )


private fun getExitTime(exitDistance: Vector3, delta: Vector3) =
    Vector3(
        if (delta.x != 0f) {
            exitDistance.x / delta.x
        } else {
            Float.POSITIVE_INFINITY
        },

        if (delta.y != 0f) {
            exitDistance.y / delta.y
        } else {
            Float.POSITIVE_INFINITY
        },

        if (delta.z != 0f) {
            exitDistance.z / delta.z
        } else {
            Float.POSITIVE_INFINITY
        }
    )
