package bke.iso.engine.collision

import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
 * This code was adapted from tesselode's excellent swept collision detection code found here:
 * https://gist.github.com/tesselode/e1bcf22f2c47baaedcfc472e78cac55e
 *
 * @param a The first box
 * @param b The second box
 * @param delta The first box's movement delta
 * @return A [SweptCollision] if a collision occurred, otherwise null
 */
fun sweepTest(a: Box, b: Box, delta: Vector3): SweptCollision? {
    val dx = delta.x
    val dy = delta.y
    val dz = delta.z

    entryTime.setZero()
    entryDistance.setZero()
    exitTime.setZero()
    exitDistance.setZero()

    /**
     * First, we calculate the entry and exit times and distances for each axis, starting with the X-axis.
     * A collision only occurs if box A overlaps box B on all three axes at some point during box A's movement.
     *
     * Therefore, if at any point we determine that box A won't overlap box B on a particular axis, we can conclude
     * that no collision will occur.
     *
     * The most common scenario where this happens is that on a particular axis, A doesn't overlap B and isn't moving.
     * Since A never moves, a collision will never occur on that axis. Thus, A will never collide with B.
     */
    if (dx == 0f) {
        if (a.min.x < b.max.x && b.min.x < a.max.x) {
            // Box A already overlaps B on this axis, so we'll say it started overlapping an infinite amount of time ago.
            entryTime.x = Float.NEGATIVE_INFINITY
            // Since A isn't moving, we'll use +infinity here to represent that A will never stop overlapping B.
            exitTime.x = Float.POSITIVE_INFINITY
        } else {
            // See above: If A is not moving and doesn't overlap B, A will never collide with B on this axis.
            return null
        }
    } else {
        entryDistance.x =
            if (dx > 0f) {
                b.min.x - a.max.x
            } else {
                a.min.x - b.max.x
            }

        exitDistance.x =
            if (dx > 0f) {
                b.max.x - a.min.x
            } else {
                a.max.x - b.min.x
            }

        entryTime.x = entryDistance.x / abs(dx)
        exitTime.x = exitDistance.x / abs(dx)
    }

    // Calculate entry and exit times and distances for the y-axis
    if (dy == 0f) {
        if (a.min.y < b.max.y && b.min.y < a.max.y) {
            entryTime.y = Float.NEGATIVE_INFINITY
            exitTime.y = Float.POSITIVE_INFINITY
        } else {
            return null
        }
    } else {
        entryDistance.y =
            if (dy > 0f) {
                b.min.y - a.max.y
            } else {
                a.min.y - b.max.y
            }

        exitDistance.y =
            if (dy > 0f) {
                b.max.y - a.min.y
            } else {
                a.max.y - b.min.y
            }

        entryTime.y = entryDistance.y / abs(dy)
        exitTime.y = exitDistance.y / abs(dy)
    }

    // Calculate entry and exit times and distances for the z-axis
    if (dz == 0f) {
        if (a.min.z < b.max.z && b.min.z < a.max.z) {
            entryTime.z = Float.NEGATIVE_INFINITY
            exitTime.z = Float.POSITIVE_INFINITY
        } else {
            return null
        }
    } else {
        entryDistance.z =
            if (dz > 0f) {
                b.min.z - a.max.z
            } else {
                a.min.z - b.max.z
            }

        exitDistance.z =
            if (dz > 0f) {
                b.max.z - a.min.z
            } else {
                a.max.z - b.min.z
            }

        entryTime.z = entryDistance.z / abs(dz)
        exitTime.z = exitDistance.z / abs(dz)
    }

    val maxEntryTime = max(entryTime.x, max(entryTime.y, entryTime.z))
    val minExitTime = min(exitTime.x, min(exitTime.y, exitTime.z))

    /**
     * Each axis produces a time interval where A and B overlap on that axis:
     * X: [entryTime.X, exitTime.X], Y: [entryTime.Y, exitTime.Y], Z: [entryTime.Z, exitTime.Z].
     *
     * A real collision happens only when all three overlaps happen at the same time.
     * That simultaneous overlap is the intersection of the intervals:
     * [max(entryTime.X, entryTime.Y, entryTime.Z), min(exitTime.X, exitTime.Y, exitTime.Z)].
     *
     * If maxEntryTime > minExitTime, the intersection is empty, which means there is no collision in this sweep.
     * Otherwise, the first contact time is maxEntryTime, and the hit normal is from the axis
     * that contributed maxEntryTime. Axes with zero velocity are handled via -∞/+∞.
     */
    if (maxEntryTime > minExitTime) {
        return null
    }

    // if the max entry time is outside the 0-1 range, that means no collision occurred
    if (maxEntryTime < 0f || maxEntryTime > 1f) {
        return null
    }

    val hitNormal = Vector3()
    when {
        entryTime.x >= entryTime.y && entryTime.x >= entryTime.z -> {
            hitNormal.x =
                if (dx > 0f) {
                    -1f
                } else {
                    1f
                }
        }

        entryTime.y >= entryTime.z -> {
            hitNormal.y =
                if (dy > 0f) {
                    -1f
                } else {
                    1f
                }
        }

        else -> hitNormal.z =
            if (dz > 0f) {
                -1f
            } else {
                1f
            }
    }

    return SweptCollision(maxEntryTime, hitNormal)
}

/**
 * Represents the time it will take for box A to begin overlapping box B on a particular axis.
 * Used as a temp variable to avoid extra allocations.
 */
private val entryTime = Vector3()

/**
 * Represents the distance between the closest sides of boxes A and B on a particular axis.
 * Box A would need to travel this distance to begin overlapping box B.
 * Used as a temp variable to avoid extra allocations.
 */
private val entryDistance = Vector3()

/**
 * Represents the time it will take for box A to stop overlapping box B on a particular axis.
 * Used as a temp variable to avoid extra allocations.
 */
private val exitTime = Vector3()

/**
 * Represents the distance between the farthest sides of boxes A and B on a particular axis.
 * Box A would need to travel this distance to stop overlapping box B.
 * Used as a temp variable to avoid extra allocations.
 */
private val exitDistance = Vector3()
