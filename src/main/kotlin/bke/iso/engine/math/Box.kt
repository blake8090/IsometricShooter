package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Segment

data class Box(
    val pos: Vector3,
    val width: Float,
    val length: Float,
    val height: Float
) {
    constructor(min: Vector3, max: Vector3) : this(
        Vector3(
            min.x + ((max.x - min.x) / 2f),
            min.y + ((max.y - min.y) / 2f),
            min.z
        ),
        max.x - min.x,
        max.y - min.y,
        max.z - min.z
    )

    /**
     * Minimum point of the box; the bottom-left corner of the closest face.
     */
    val min = Vector3(
        pos.x - (width / 2f),
        pos.y - (length / 2f),
        pos.z
    )

    /**
     * Maximum point of the box; the top-right corner of the farthest face.
     */
    val max = Vector3(
        pos.x + (width / 2f),
        pos.y + (length / 2f),
        pos.z + height
    )

    val center = Vector3(
        pos.x,
        pos.y,
        pos.z + (height / 2f)
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

    fun intersects(other: Box): Boolean {
        return min.x < other.max.x &&
                max.x > other.min.x &&
                min.y < other.max.y &&
                max.y > other.min.y &&
                min.z < other.max.z &&
                max.z > other.min.z
    }

    fun project(dx: Float, dy: Float, dz: Float): Box {
        val min = Vector3(min)
        val max = Vector3(max)
        if (dx < 0) min.x += dx else max.x += dx
        if (dy < 0) min.y += dy else max.y += dy
        if (dz < 0) min.z += dz else max.z += dz
        return Box(min, max)
    }

    fun dst(other: Box) =
        center.dst(other.center)
}
