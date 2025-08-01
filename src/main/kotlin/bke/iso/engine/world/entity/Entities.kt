package bke.iso.engine.world.entity

import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import com.badlogic.gdx.math.Vector3
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KClass

class Entities(private val events: Events) {

    private val grid = Grid()
    private val entityIdLength = 12
    private val entityIdSymbols: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    operator fun iterator() =
        grid.entities.iterator()

    fun create(location: Location, vararg components: Component): Entity =
        create(
            generateEntityId(),
            location.x.toFloat(),
            location.y.toFloat(),
            location.z.toFloat(),
            *components
        )

    fun create(pos: Vector3, vararg components: Component): Entity =
        create(
            generateEntityId(),
            pos.x,
            pos.y,
            pos.z,
            *components
        )

    fun create(
        id: String,
        x: Float,
        y: Float,
        z: Float,
        vararg components: Component
    ): Entity {
        val entity = Entity(id, this::onMove)

        for (component in components) {
            entity.add(component)
        }

        entity.moveTo(x, y, z)
        events.fire(CreatedEvent(entity))
        return entity
    }

    private fun generateEntityId() =
        List(entityIdLength) { entityIdSymbols.random() }.joinToString("")

    fun delete(entity: Entity) {
        grid.delete(entity)
    }

    fun clear() {
        grid.clear()
    }

    fun get(id: String): Entity =
        grid.entities
            .find { entity -> entity.id == id }
            ?: throw IllegalArgumentException("No entity found with id $id")

    fun <T : Component> each(type: KClass<out T>, action: (Entity, T) -> Unit) {
        for (entity in grid.entities.toList()) {
            val component = entity.get(type) ?: continue
            action.invoke(entity, component)
        }
    }

    inline fun <reified T : Component> each(noinline action: (Entity, T) -> Unit) =
        each(T::class, action)

    fun <T : Component> find(type: KClass<out T>): Entity? =
        grid.entities
            .find { entity ->
                entity.components.containsKey(type)
            }

    inline fun <reified T : Component> find(): Entity? =
        find(T::class)

    fun find(id: String): Entity? =
        grid.entities
            .find { entity -> entity.id == id }

    fun <T : Component> findAll(type: KClass<out T>): List<Entity> =
        grid.entities
            .filter { entity -> entity.components.containsKey(type) }

    inline fun <reified T : Component> findAll(): List<Entity> =
        findAll(T::class)

    fun findAllAt(point: Vector3): Set<Entity> =
        grid[Location(point)].toSet()

    fun findAllIn(box: Box): Set<Entity> {
        val minX = floor(box.min.x).toInt()
        val minY = floor(box.min.y).toInt()
        val minZ = floor(box.min.z).toInt()

        val maxX = ceil(box.max.x).toInt()
        val maxY = ceil(box.max.y).toInt()
        val maxZ = ceil(box.max.z).toInt()

        val objects = mutableSetOf<Entity>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    objects.addAll(grid[Location(x, y, z)])
                }
            }
        }
        return objects
    }

    /**
     * Manually update an entity's location in the grid.
     *
     * This should be used in situations where an entity's [bke.iso.engine.collision.Collider] component was updated
     * but the entity hasn't moved yet, for example when placing or editing reference entities in the editor.
     */
    fun updateGrid(entity: Entity) {
        grid.update(entity)
    }

    private fun onMove(entity: Entity) =
        grid.update(entity)

    data class CreatedEvent(val entity: Entity) : Event
}
