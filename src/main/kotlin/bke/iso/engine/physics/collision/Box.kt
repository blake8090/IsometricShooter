package bke.iso.engine.physics.collision

import bke.iso.engine.math.getBottomLeft
import bke.iso.engine.math.getBottomRight
import bke.iso.engine.math.getTopLeft
import bke.iso.engine.math.getTopRight
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment

data class Box(
    val center: Vector3,
    val width: Float,
    val length: Float,
    val height: Float
) {

    private val bottomCorner = Vector3(
        center.x - (width / 2f),
        center.y - (length / 2f),
        center.z
    )

    private val topCorner = Vector3(
        center.x + (width / 2f),
        center.y + (length / 2f),
        center.z + height
    )

    /**
     * Returns the minimum point of the box.
     *
     * This would be the bottom-left corner of the closest face.
     */
    fun getMin() = bottomCorner

    /**
     * Returns the minimum point of the box.
     *
     * This would be the top-right corner of the farthest face.
     */
    fun getMax() = topCorner

    fun getSegments(): List<Segment> {
        val rect = Rectangle(
            bottomCorner.x,
            bottomCorner.y,
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
