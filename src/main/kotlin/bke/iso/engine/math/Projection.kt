package bke.iso.engine.math

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
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

fun toScreen(x: Float, y: Float, z: Float = 0f) =
    Vector3(
        (x + y) * HALF_TILE_WIDTH,
        (y - x) * HALF_TILE_HEIGHT,
        0f
    )

fun toScreen(vector: Vector3) =
    toScreen(vector.x, vector.y)

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

fun toWorld(pos: Vector3): Vector3 {
    val w = HALF_TILE_WIDTH
    val h = HALF_TILE_HEIGHT
    val mapX = (pos.x / w) - (pos.y / h)
    val mapY = (pos.y / h) + (pos.x / w)
    return Vector3(
        mapX / 2,
        mapY / 2,
        0f
    )
}
