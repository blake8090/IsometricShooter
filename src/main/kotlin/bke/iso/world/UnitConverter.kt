package bke.iso.world

import bke.iso.world.entity.PositionComponent
import com.badlogic.gdx.math.Vector2

// TODO: Add unit tests
class UnitConverter(private val tileWidth: Int, private val tileHeight: Int) {
    fun tileToScreen(location: Location): Vector2 =
        worldToScreen(location).apply {
            x -= tileWidth / 2
            y -= tileHeight
        }

    fun worldToScreen(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vector2 {
        // In the isometric grid, the X-axis moves towards the bottom right, and the Y-axis to the bottom left
        val screenPos = Vector2(
            (y - x) * -1,
            (y + x) * -1,
        )
        screenPos.x *= (tileWidth / 2)
        screenPos.y *= (tileHeight / 2)
        return screenPos
    }

    fun worldToScreen(pos: PositionComponent): Vector2 =
        worldToScreen(pos.x, pos.y)

    fun worldToScreen(location: Location): Vector2 =
        worldToScreen(location.x.toFloat(), location.y.toFloat())
}
