package bke.iso.engine.world.entity

import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import com.badlogic.gdx.math.Vector3
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KClass

class Actors(private val events: Events) {

    private val grid = Grid()
    private val actorIdLength = 12
    private val actorIdSymbols: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    operator fun iterator() =
        grid.actors.iterator()

    fun create(location: Location, vararg components: Component): Actor =
        create(
            generateActorId(),
            location.x.toFloat(),
            location.y.toFloat(),
            location.z.toFloat(),
            *components
        )

    fun create(pos: Vector3, vararg components: Component): Actor =
        create(
            generateActorId(),
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
    ): Actor {
        val actor = Actor(id, this::onMove)

        for (component in components) {
            actor.add(component)
        }

        actor.moveTo(x, y, z)
        events.fire(CreatedEvent(actor))
        return actor
    }

    private fun generateActorId() =
        List(actorIdLength) { actorIdSymbols.random() }.joinToString("")

    fun delete(actor: Actor) {
        grid.delete(actor)
    }

    fun clear() {
        grid.clear()
    }

    fun get(id: String): Actor =
        grid.actors
            .find { actor -> actor.id == id }
            ?: throw IllegalArgumentException("No actor found with id $id")

    fun <T : Component> each(type: KClass<out T>, action: (Actor, T) -> Unit) {
        for (actor in grid.actors.toList()) {
            val component = actor.get(type) ?: continue
            action.invoke(actor, component)
        }
    }

    inline fun <reified T : Component> each(noinline action: (Actor, T) -> Unit) =
        each(T::class, action)

    fun <T : Component> find(type: KClass<out T>): Actor? =
        grid.actors
            .find { actor ->
                actor.components.containsKey(type)
            }

    inline fun <reified T : Component> find(): Actor? =
        find(T::class)

    fun find(id: String): Actor? =
        grid.actors
            .find { actor -> actor.id == id }

    fun <T : Component> findAll(type: KClass<out T>): List<Actor> =
        grid.actors
            .filter { actor -> actor.components.containsKey(type) }

    inline fun <reified T : Component> findAll(): List<Actor> =
        findAll(T::class)

    fun findAllAt(point: Vector3): Set<Actor> =
        grid[Location(point)].toSet()

    fun findAllIn(box: Box): Set<Actor> {
        val minX = floor(box.min.x).toInt()
        val minY = floor(box.min.y).toInt()
        val minZ = floor(box.min.z).toInt()

        val maxX = ceil(box.max.x).toInt()
        val maxY = ceil(box.max.y).toInt()
        val maxZ = ceil(box.max.z).toInt()

        val objects = mutableSetOf<Actor>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    objects.addAll(grid[Location(x, y, z)])
                }
            }
        }
        return objects
    }

    private fun onMove(actor: Actor) =
        grid.update(actor)

    data class CreatedEvent(val actor: Actor) : Event
}
