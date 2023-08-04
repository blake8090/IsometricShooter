package bke.iso.v2.engine.world

import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import bke.iso.v2.engine.render.Sprite
import com.badlogic.gdx.math.Vector3
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
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
        val actor = Actor(id, x, y, z, this::onMove)
        components.forEach { component -> actor.components[component::class] = component }
        return actor
    }

    private fun onMove(actor: Actor) =
        grid.move(actor)

    fun deleteActor(actor: Actor) {
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
            .associateWith { actor -> actor.components[type] }
            .mapNotNull { (actor, component) ->
                component ?: return@mapNotNull null
                actor to component
            }
            .toSet()

    fun <T : Component> findActorWith(type: KClass<out T>): Pair<Actor, T>? =
        findActorsWith(type).firstOrNull()

    inline fun <reified T : Component> findActorWith() =
        findActorWith(T::class)

    fun getObjectsInArea(box: Box): Set<GameObject> {
        val min = Vector3(
            min(box.min.x, box.max.x),
            min(box.min.y, box.max.y),
            min(box.min.z, box.max.z),
        )
        val max = Vector3(
            max(box.min.x, box.max.x),
            max(box.min.y, box.max.y),
            max(box.min.z, box.max.z),
        )

        val minX = floor(min.x).toInt()
        val maxX = ceil(max.x).toInt()

        val minY = floor(min.y).toInt()
        val maxY = ceil(max.y).toInt()

        val minZ = floor(min.z).toInt()
        val maxZ = ceil(max.z).toInt()

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
