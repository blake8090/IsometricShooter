package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.max
import kotlin.math.min

data class Box(
    /**
     * Center point of the box
     */
    val pos: Vector3,
    /**
     * Box dimensions
     */
    val size: Vector3
) {

    val min: Vector3 = Vector3(
        pos.x - (size.x / 2f),
        pos.y - (size.y / 2f),
        pos.z - (size.z / 2f)
    )

    val max: Vector3 = Vector3(
        pos.x + (size.x / 2f),
        pos.y + (size.y / 2f),
        pos.z + (size.z / 2f)
    )

    val segments: List<Segment> = listOf(
        // top
        Segment(Vector3(min.x, min.y, max.z), Vector3(min.x, max.y, max.z)),
        Segment(Vector3(min.x, max.y, max.z), Vector3(max.x, max.y, max.z)),
        Segment(Vector3(max.x, max.y, max.z), Vector3(max.x, min.y, max.z)),
        Segment(Vector3(max.x, min.y, max.z), Vector3(min.x, min.y, max.z)),

        // bottom
        Segment(Vector3(min.x, min.y, min.z), Vector3(min.x, max.y, min.z)),
        Segment(Vector3(min.x, max.y, min.z), Vector3(max.x, max.y, min.z)),
        Segment(Vector3(max.x, max.y, min.z), Vector3(max.x, min.y, min.z)),
        Segment(Vector3(max.x, min.y, min.z), Vector3(min.x, min.y, min.z)),

        // corners
        Segment(Vector3(min.x, min.y, min.z), Vector3(min.x, min.y, max.z)),
        Segment(Vector3(max.x, min.y, min.z), Vector3(max.x, min.y, max.z)),
        Segment(Vector3(min.x, max.y, min.z), Vector3(min.x, max.y, max.z)),
        Segment(Vector3(max.x, max.y, min.z), Vector3(max.x, max.y, max.z))
    )

    val faces: List<BoundingBox> = listOf(
        // top
        BoundingBox(Vector3(min.x, min.y, max.z), Vector3(max.x, max.y, max.z)),
        // bottom
        BoundingBox(Vector3(min.x, min.y, min.z), Vector3(max.x, max.y, min.z)),
        // left
        BoundingBox(Vector3(min.x, min.y, min.z), Vector3(min.x, max.y, max.z)),
        // right
        BoundingBox(Vector3(max.x, min.y, min.z), Vector3(max.x, max.y, max.z)),
        // front
        BoundingBox(Vector3(min.x, min.y, min.z), Vector3(max.x, min.y, max.z)),
        // back
        BoundingBox(Vector3(min.x, max.y, min.z), Vector3(max.x, max.y, max.z))
    )

    fun intersects(box: Box): Boolean {
        return min.x < box.max.x &&
                max.x > box.min.x &&
                min.y < box.max.y &&
                max.y > box.min.y &&
                min.z < box.max.z &&
                max.z > box.min.z
    }

    fun getOverlapArea(box: Box): Float {
        val dx = min(max.x, box.max.x) - max(min.x, box.min.x)
        val dy = min(max.y, box.max.y) - max(min.y, box.min.y)
        val dz = min(max.z, box.max.z) - max(min.z, box.min.z)
        return if (dx >= 0f && dy >= 0f && dz >= 0f) {
            dx * dy * dz
        } else {
            0f
        }
    }

    fun expand(dx: Float, dy: Float, dz: Float): Box {
        val min = Vector3(min)
        val max = Vector3(max)
        if (dx < 0) min.x += dx else max.x += dx
        if (dy < 0) min.y += dy else max.y += dy
        if (dz < 0) min.z += dz else max.z += dz
        return fromMinMax(min, max)
    }

    fun dst(box: Box): Float =
        pos.dst(box.pos)

    companion object {
        fun fromMinMax(min: Vector3, max: Vector3): Box {
            val size = Vector3(max).sub(min)
            val center = Vector3(size)
                .scl(0.5f)
                .add(min)
            return Box(center, size)
        }

        fun fromMinMax(segment: Segment): Box {
            val min = Vector3(
                min(segment.a.x, segment.b.x),
                min(segment.a.y, segment.b.y),
                min(segment.a.z, segment.b.z)
            )
            val max = Vector3(
                max(segment.a.x, segment.b.x),
                max(segment.a.y, segment.b.y),
                max(segment.a.z, segment.b.z)
            )
            return fromMinMax(min, max)
        }
    }
}
