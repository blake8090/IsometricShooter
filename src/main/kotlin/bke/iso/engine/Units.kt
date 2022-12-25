package bke.iso.engine

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

// TODO: deprecate this file in favor of the following:
//  - create "math" package
//  - move screen/world conversion code to "Projection.kt"

// TODO: Move to own file, as new constructors will need to be added
data class Location(val x: Int, val y: Int) {
    constructor(x: Float, y: Float) :
            this(
                floor(x).toInt(),
                floor(y).toInt()
            )

    fun toVector2() =
        Vector2(x.toFloat(), y.toFloat())
}

// TODO: Add unit tests
object Units {
    private const val tileWidth: Int = 64
    private const val tileHeight: Int = 32

    /**
     * Converts a location on the world grid to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(location: Location): Vector2 {
        return worldToScreen(location.x.toFloat(), location.y.toFloat())
    }

    /**
     * Converts a precise position in the world to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(x: Float, y: Float) =
        Vector2(
            (x + y) * (tileWidth / 2),
            (y - x) * (tileHeight / 2)
        )

    /**
     * Converts a precise position in the world to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(pos: Vector2): Vector2 =
        worldToScreen(pos.x, pos.y)

    fun toScreen(polygon: Polygon): Polygon {
        val vertices = polygon.vertices
            .toList()
            .zipWithNext()
            .map { pair -> worldToScreen(pair.first, pair.second) }
            .flatMap { vector -> listOf(vector.x, vector.y) }
            .toFloatArray()
        val screenPolygon = Polygon(vertices)
        val pos = worldToScreen(polygon.x, polygon.y)
        screenPolygon.setPosition(pos.x, pos.y)
        return screenPolygon
    }

    fun toWorld(pos: Vector2): Vector2 {
        val w = tileWidth / 2
        val h = tileHeight / 2
        val mapX = (pos.x / w) - (pos.y / h)
        val mapY = (pos.y / h) + (pos.x / w)
        return Vector2(
            mapX / 2,
            mapY / 2
        )
    }
}
