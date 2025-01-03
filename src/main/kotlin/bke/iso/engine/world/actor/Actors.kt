package bke.iso.engine.world.actor

import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.math.Location
import bke.iso.engine.world.Grid
import com.badlogic.gdx.math.Vector3
import kotlin.reflect.KClass

private const val ID_LENGTH = 6

class Actors(
    private val grid: Grid,
    private val events: Events
) {

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

    fun get(id: String): Actor =
        grid.getObjects()
            .filterIsInstance<Actor>()
            .find { actor -> actor.id == id }
            ?: throw IllegalArgumentException("No actor found with id $id")

    fun <T : Component> each(type: KClass<out T>, action: (Actor, T) -> Unit) {
        val actors = grid.getObjects().filterIsInstance<Actor>()
        for (actor in actors) {
            val component = actor.get(type) ?: continue
            action.invoke(actor, component)
        }
    }

    inline fun <reified T : Component> each(noinline action: (Actor, T) -> Unit) =
        each(T::class, action)

    fun <T : Component> find(type: KClass<out T>): Actor? =
        grid.getObjects()
            .filterIsInstance<Actor>()
            .find { actor ->
                actor.components.containsKey(type)
            }

    inline fun <reified T : Component> find(): Actor? =
        find(T::class)

    fun find(id: String): Actor? =
        grid.getObjects()
            .filterIsInstance<Actor>()
            .find { actor -> actor.id == id }

    fun <T : Component> findAll(type: KClass<out T>): List<Actor> =
        grid.getObjects()
            .filterIsInstance<Actor>()
            .filter { actor -> actor.components.containsKey(type) }

    inline fun <reified T : Component> findAll(): List<Actor> =
        findAll(T::class)

    private fun onMove(actor: Actor) =
        grid.update(actor)

    private fun generateActorId(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(ID_LENGTH) { charPool.random() }.joinToString("")
    }

    data class CreatedEvent(val actor: Actor) : Event
}
