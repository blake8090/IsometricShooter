package bke.iso.v2.engine.world

import com.badlogic.gdx.math.Vector2

// TODO: Add unit tests
class Units(private val tileWidth: Int, private val tileHeight: Int) {
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
}
