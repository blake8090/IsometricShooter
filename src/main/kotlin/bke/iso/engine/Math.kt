package bke.iso.engine

import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.sqrt

const val TILE_WIDTH = 64
const val TILE_HEIGHT = 32
const val HALF_TILE_WIDTH = TILE_WIDTH / 2
const val HALF_TILE_HEIGHT = TILE_HEIGHT / 2

/**
 * Two to one ratio (64/32 = 2)
 */
fun getIsometricRatio() =
    sqrt(TILE_WIDTH.toFloat() / TILE_HEIGHT.toFloat())

fun toScreen(x: Float, y: Float) =
    Vector2(
        (x + y) * HALF_TILE_WIDTH,
        (y - x) * HALF_TILE_HEIGHT
    )

/**
 * Given a [Circle] defined in world units, returns a [Circle] defined in screen units.
 */
fun toScreen(circle: Circle): Circle =
    // TODO: should radius be converted as well?
    Circle(
        toScreen(circle.x, circle.y),
        circle.radius
    )

// TODO: investigate using this approach instead:
//  - return a Rectangle
//  - add a method to convert a Rectangle to a Polygon
/**
 * Converts a [Rectangle] defined in world units, to a [Polygon] with vertices defined in screen units.
 */
fun toScreen(rect: Rectangle): Polygon {
    val vertices = listOf(
        toScreen(rect.x, rect.y),
        toScreen(rect.x, rect.y + rect.height),
        toScreen(rect.x + rect.width, rect.y + rect.height),
        toScreen(rect.x + rect.width, rect.y)
    )
    return Polygon(
        vertices
            .flatMap { listOf(it.x, it.y) }
            .toFloatArray()
    )
}

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
