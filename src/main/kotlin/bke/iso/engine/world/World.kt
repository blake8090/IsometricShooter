package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.Box2
import bke.iso.engine.render.Sprite
import java.util.UUID
import kotlin.reflect.KClass

abstract class GameObject

class World(override val game: Game) : Module() {

    private val grid = Grid()
    private val deletedObjects = mutableSetOf<Actor>()

    val objects: Set<GameObject>
        get() = grid.getAll()

    override fun update(deltaTime: Float) {
        deletedObjects.forEach(grid::remove)
        deletedObjects.clear()
    }

    fun newActor(
        x: Float, y: Float, z: Float,
        vararg components: Component,
        id: UUID = UUID.randomUUID()
    ): Actor {
        val actor = Actor(id, this::onMove)
        components.forEach(actor::add)
        actor.moveTo(x, y, z)
        return actor
    }

    private fun onMove(actor: Actor) =
        grid.update(actor)

    fun delete(actor: Actor) {
        deletedObjects.add(actor)
    }

    fun setTile(location: Location, sprite: Sprite, solid: Boolean = false) =
        grid.setTile(location, sprite, solid)

    fun <T : Component> actorsWith(type: KClass<out T>, action: (Actor, T) -> Unit) {
        for ((actor, component) in findActorsWith(type)) {
            action.invoke(actor, component)
        }
    }

    inline fun <reified T : Component> actorsWith(noinline action: (Actor, T) -> Unit) =
        actorsWith(T::class, action)

    private fun <T : Component> findActorsWith(type: KClass<out T>): Set<Pair<Actor, T>> =
        grid.getAllActors()
            .mapNotNullTo(mutableSetOf()) { actor ->
                actor.get(type)
                    ?.let { component -> actor to component }
            }

    fun <T : Component> findActorWith(type: KClass<out T>): Pair<Actor, T>? =
        findActorsWith(type).firstOrNull()

    inline fun <reified T : Component> findActorWith() =
        findActorWith(T::class)

    fun getObjectsInArea(box: Box2): Set<GameObject> {
        val minX = box.min.x.toInt()
        val minY = box.min.y.toInt()
        val minZ = box.min.z.toInt()

        val maxX = box.max.x.toInt()
        val maxY = box.max.y.toInt()
        val maxZ = box.max.z.toInt()

        val objects = mutableSetOf<GameObject>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    objects.addAll(grid.getAll(Location(x, y, z)))
                }
            }
        }
        return objects
    }
}
