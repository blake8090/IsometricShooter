package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.engine.entity.Component
import bke.iso.engine.entity.Entities
import java.util.UUID
import kotlin.math.ceil

data class Velocity(
    val dx: Float = 0f,
    val dy: Float = 0f
) : Component()

/**
 * Defines a collision box as a 2D rectangle.
 * @param x x position of the bottom-left corner of the box, relative to an entity's origin
 * @param y y position of the bottom-left corner of the box, relative to an entity's origin
 * @param width width of the box
 * @param length length of the box
 */
data class CollisionBox(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float,
    val length: Float
)

data class Collision(
    val box: CollisionBox,
    val static: Boolean = false
) : Component()

@Service
class Physics(private val entities: Entities) {
    fun update(deltaTime: Float) {
        entities.withComponent(Velocity::class) { entity, velocity ->
            val pos = entity.getPos()

            entity.setPos(
                pos.x + (velocity.dx * deltaTime),
                pos.y + (velocity.dy * deltaTime)
            )
        }
    }

    private fun getEntitiesInRange(
        x: Float,
        y: Float,
        width: Float,
        length: Float
    ): Set<UUID> {
        /*
        location steps:
        ceil((maxX - minX) + 1)
         */
        val maxX = x + width
        val stepsX = ceil(maxX - x).toInt()

        val maxY = y + length
        val stepsY = ceil(maxY - y).toInt()

        val ids = mutableSetOf<UUID>()
        for (locationY in 0..stepsY) {
            for (locationX in 0..stepsX) {
                entities.findAllInLocation(Location(locationX, locationY))
                    .forEach(ids::add)
            }
        }
        return ids
    }
}
