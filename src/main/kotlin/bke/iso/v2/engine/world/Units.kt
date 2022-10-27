package bke.iso.v2.engine.world

import com.badlogic.gdx.math.Vector2

// TODO: Add unit tests
class Units(private val tileWidth: Int, private val tileHeight: Int) {
    /**
     * Converts a precise position on the world grid to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(x: Float = 0f, y: Float = 0f): Vector2 {
        // In the isometric grid, the X-axis moves towards the bottom right, and the Y-axis to the bottom left
        val screenPos = Vector2(
            (y - x) * -1,
            (y + x) * -1,
        )
        screenPos.x *= (tileWidth / 2)
        screenPos.y *= (tileHeight / 2)
        return screenPos
    }

    /**
     * Converts a location on the world grid to a [Vector2] for use with the LibGDX API.
     */
    fun worldToScreen(location: Location): Vector2 =
        worldToScreen(location.x.toFloat(), location.y.toFloat())

    /**
     * Converts a location on the world grid to a [Vector2] for use with the LibGDX API.
     *
     * Additional offsets are applied to ensure that tiles render properly alongside entities.
     */
    fun tileToScreen(location: Location): Vector2 =
        worldToScreen(location).apply {
            x -= tileWidth / 2
            y -= tileHeight
        }
}
