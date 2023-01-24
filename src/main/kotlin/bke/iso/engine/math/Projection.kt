package bke.iso.engine.math

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.sqrt

/**
 * Width of one tile in pixels
 */
const val TILE_SIZE_X = 64

/**
 * Length of one tile in pixels
 */
const val TILE_SIZE_Y = 32

/**
 * Height of one tile in pixels
 */
const val TILE_SIZE_Z = 64

/**
 * Two to one ratio (64/32 = 2)
 */
fun getIsometricRatio() =
    sqrt(TILE_SIZE_X.toFloat() / TILE_SIZE_Y.toFloat())

fun toScreen(x: Float, y: Float, z: Float): Vector2  {
    val scaleX = TILE_SIZE_X / 2
    val scaleY = TILE_SIZE_Y / 2
    val height =  z * TILE_SIZE_Z
    return Vector2(
        (x + y) * scaleX,
        (y - x) * scaleY + height
    )
}

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
    val w = TILE_SIZE_X / 2
    val h = TILE_SIZE_Y / 2
    val mapX = (screenPos.x / w) - (screenPos.y / h)
    val mapY = (screenPos.y / h) + (screenPos.x / w)
    return Vector3(
        mapX / 2,
        mapY / 2,
        0f
    )
}
