package bke.iso.v2.engine.world

import com.badlogic.gdx.math.Vector2
import java.util.*

class Entities(private val grid: WorldGrid) {
    private val posById = mutableMapOf<UUID, Vector2>()

    fun create(): UUID {
        val id = UUID.randomUUID()
        setPos(id, 0f, 0f)
        return id
    }

    /**
     * If the entity ID exists, returns the entity's precise position.
     */
    fun getPos(id: UUID): Vector2? =
        posById[id]

    fun setPos(id: UUID, x: Float, y: Float) {
        posById[id] = Vector2(x, y)

        val location = Location(x.toInt(), y.toInt())
        grid.setEntityLocation(id, location)
    }

    fun move(id: UUID, dx: Float, dy: Float) {
        val pos = posById[id] ?: return
        setPos(id, pos.x + dx, pos.y + dy)
    }
}
