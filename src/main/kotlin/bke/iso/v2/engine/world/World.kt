package bke.iso.v2.engine.world

import bke.iso.engine.entity.Component
import bke.iso.engine.log
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import com.badlogic.gdx.math.Vector3
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class World(game: Game) : Module(game) {

    private val grid = Grid()
    private val deletedObjects = mutableSetOf<GameObject>()

    val objects: Set<GameObject>
        get() = grid.objects

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

    fun delete(gameObject: GameObject) {
        deletedObjects.add(gameObject)
    }

    fun setTile(location: Location, sprite: Sprite, solid: Boolean = false) =
        Tile(UUID.randomUUID(), location, sprite, solid, ::onMove)

    private fun onMove(gameObject: GameObject) {
        log.trace("moving")
        grid.move(gameObject, Location(gameObject.pos))
    }

    fun <T : Component> actorsWith(type: KClass<out T>, action: (Actor, T) -> Unit) {
        for (actor in grid.objects.filterIsInstance<Actor>()) {
            val component = actor.components[type] ?: continue
            action.invoke(actor, component)
        }
    }

    inline fun <reified T : Component> actorsWith(noinline action: (Actor, T) -> Unit) =
        actorsWith(T::class, action)

    fun <T : Component> findActorWith(type: KClass<out T>): Pair<Actor, T>? =
       grid.objects
           .filterIsInstance<Actor>()
           .mapNotNull { actor ->
               val component = actor.components[type] ?: return@mapNotNull null
               actor to component
           }
           .firstOrNull()

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
