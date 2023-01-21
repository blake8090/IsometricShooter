package bke.iso.engine.math

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
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

fun Vector3.toVector2() =
    Vector2(x, y)
