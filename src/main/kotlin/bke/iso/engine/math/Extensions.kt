package bke.iso.engine.math

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.collision.Segment

fun Rectangle.getEdges() =
    listOf(
        // top
        Segment(
            Vector3(x, y + height, 0f),
            Vector3(x + width, y + height, 0f)
        ),

        // bottom
        Segment(
            Vector3(x, y, 0f),
            Vector3(x + width, y, 0f)
        ),

        // left
        Segment(
            Vector3(x, y, 0f),
            Vector3(x, y + height, 0f)
        ),

        // right
        Segment(
            Vector3(x + width, y, 0f),
            Vector3(x + width, y + height, 0f)
        ),
    )

fun Rectangle.getBottomLeft() =
    Vector2(x, y)

fun Rectangle.getBottomRight() =
    Vector2(x + width, y)

fun Rectangle.getTopLeft() =
    Vector2(x, y + height)

fun Rectangle.getTopRight() =
    Vector2(x + width, y + height)

fun Vector3.toVector2() =
    Vector2(x, y)

fun Ray.getEndPoint(distance: Float): Vector3 {
    val end = Vector3()
    getEndPoint(end, distance)
    return end
}
