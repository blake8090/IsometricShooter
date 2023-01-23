package bke.iso.engine.math

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
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

fun toScreen(x: Float, y: Float, z: Float) =
    Vector2(
        (x + y) * HALF_TILE_WIDTH,
        (y - x) * HALF_TILE_HEIGHT + (z * 64)
    )

fun toScreen(x: Float, y: Float) =
    toScreen(x, y, 0f)

fun toScreen(worldPos: Vector3) =
    toScreen(worldPos.x, worldPos.y, worldPos.z)

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

fun toWorld(screenPos: Vector2): Vector3 {
    val w = HALF_TILE_WIDTH
    val h = HALF_TILE_HEIGHT
    val mapX = (screenPos.x / w) - (screenPos.y / h)
    val mapY = (screenPos.y / h) + (screenPos.x / w)
    return Vector3(
        mapX / 2,
        mapY / 2,
        0f
    )
}
