package bke.iso.engine

import bke.iso.app.service.Service
import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

data class Location(
    val x: Int,
    val y: Int
) {
    constructor(x: Float, y: Float) :
            this(
                floor(x).toInt(),
                floor(y).toInt()
            )

    constructor(pos: Vector2) : this(pos.x, pos.y)
}

// TODO: Add unit tests
@Service
class Units {
    private val tileWidth: Int = 64
    private val tileHeight: Int = 32

    /**
     * Converts a location on the world grid to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(location: Location): Vector2 {
        return worldToScreen(location.x.toFloat(), location.y.toFloat())
    }

    /**
     * Converts a precise position in the world to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(x: Float, y: Float): Vector2 {
        val x2: Float = (x * tileWidth / 2) + (y * tileWidth / 2)
        val y2: Float = (y * tileHeight / 2) - (x * tileHeight / 2)
        return Vector2(x2, y2)
    }

    /**
     * Converts a precise position in the world to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(pos: Vector2): Vector2 =
        worldToScreen(pos.x, pos.y)
}
