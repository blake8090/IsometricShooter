package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Segment

data class Box2(
    /**
     * Center point of the box
     */
    val pos: Vector3,
    /**
     * Box dimensions
     */
    val size: Vector3
) {

    val min = Vector3(
        pos.x - (size.x / 2f),
        pos.y - (size.y / 2f),
        pos.z - (size.z / 2f)
    )

    val max = Vector3(
        pos.x + (size.x / 2f),
        pos.y + (size.y / 2f),
        pos.z + (size.z / 2f)
    )

    val segments = listOf(
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

    val faces = listOf(
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

    fun intersects(box: Box2): Boolean {
        return min.x < box.max.x &&
                max.x > box.min.x &&
                min.y < box.max.y &&
                max.y > box.min.y &&
                min.z < box.max.z &&
                max.z > box.min.z
    }

    fun expand(dx: Float, dy: Float, dz: Float): Box2 {
        val min = Vector3(min)
        val max = Vector3(max)
        if (dx < 0) min.x += dx else max.x += dx
        if (dy < 0) min.y += dy else max.y += dy
        if (dz < 0) min.z += dz else max.z += dz
        return from(min, max)
    }

    fun dst(box: Box2) =
        pos.dst(box.pos)

    companion object {
        fun from(min: Vector3, max: Vector3): Box2 {
            val size = Vector3(max).sub(min)
            val center = Vector3(size)
                .scl(0.5f)
                .add(min)
            return Box2(center, size)
        }

        fun from(segment: Segment) =
            from(segment.a, segment.b)
    }
}
