package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
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

    fun project(dx: Float, dy: Float, dz: Float): Box {
        val min = Vector3(min)
        val max = Vector3(max)
        if (dx < 0) min.x += dx else max.x += dx
        if (dy < 0) min.y += dy else max.y += dy
        if (dz < 0) min.z += dz else max.z += dz
        return Box(min, max)
    }

    fun minkowskiSum(box: Box): Box {
        val w = box.width + width
        val l = box.length + length
        val h = box.height + height
        val newPos = Vector3(
            box.pos.x,
            box.pos.y,
            box.pos.z - ((h - box.height) / 2f)
        )
        return Box(newPos, w, l, h)
    }
}
