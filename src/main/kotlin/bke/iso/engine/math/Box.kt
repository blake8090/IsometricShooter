package bke.iso.engine.math

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment

data class Box(
    val center: Vector3,
    val width: Float,
    val length: Float,
    val height: Float
) {

    /**
     * Minimum point of the box; the bottom-left corner of the closest face.
     */
    val min = Vector3(
        center.x - (width / 2f),
        center.y - (length / 2f),
        center.z
    )

    /**
     * Maximum point of the box; the top-right corner of the farthest face.
     */
    val max = Vector3(
        center.x + (width / 2f),
        center.y + (length / 2f),
        center.z + height
    )

    fun getSegments(): List<Segment> {
        val rect = Rectangle(
            min.x,
            min.y,
            width,
            length
        )

        val bottomLeft = rect.getBottomLeft()
        val bottomRight = rect.getBottomRight()
        val topLeft = rect.getTopLeft()
        val topRight = rect.getTopRight()

        val bottomZ = center.z
        val topZ = center.z + height
        val segments = mutableListOf<Segment>()
        // bottom face
        segments.addAll(
            listOf(
                Segment(Vector3(bottomLeft, bottomZ), Vector3(bottomRight, bottomZ)),
                Segment(Vector3(bottomRight, bottomZ), Vector3(topRight, bottomZ)),
                Segment(Vector3(topRight, bottomZ), Vector3(topLeft, bottomZ)),
                Segment(Vector3(topLeft, bottomZ), Vector3(bottomLeft, bottomZ))
            )
        )
        // top face
        segments.addAll(
            listOf(
                Segment(Vector3(bottomLeft, topZ), Vector3(bottomRight, topZ)),
                Segment(Vector3(bottomRight, topZ), Vector3(topRight, topZ)),
                Segment(Vector3(topRight, topZ), Vector3(topLeft, topZ)),
                Segment(Vector3(topLeft, topZ), Vector3(bottomLeft, topZ))
            )
        )
        // corners
        segments.addAll(
            listOf(
                Segment(Vector3(bottomLeft, bottomZ), Vector3(bottomLeft, topZ)),
                Segment(Vector3(bottomRight, bottomZ), Vector3(bottomRight, topZ)),
                Segment(Vector3(topLeft, bottomZ), Vector3(topLeft, topZ)),
                Segment(Vector3(topRight, bottomZ), Vector3(topRight, topZ))
            )
        )
        return segments
    }
}
