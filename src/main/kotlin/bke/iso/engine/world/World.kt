package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.math.Box
import bke.iso.engine.render.Sprite
import kotlin.reflect.KClass

interface GameObject

class World(override val game: Game) : Module() {

    private val grid = Grid()
    private val actorsById = mutableMapOf<String, Actor>()
    private val deletedActors = mutableSetOf<Actor>()

    val objects: Set<GameObject>
        get() = grid.getAll()

    override fun update(deltaTime: Float) {
        for (actor in deletedActors) {
            actorsById.remove(actor.id)
            grid.remove(actor)
        }
        deletedActors.clear()
    }

    fun newActor(
        x: Float, y: Float, z: Float,
        vararg components: Component,
        id: String = generateActorId()
    ): Actor {
        val actor = Actor(id, this::onMove)
        actorsById[actor.id] = actor

        for (component in components) {
            actor.add(component)
        }

        actor.moveTo(x, y, z)
        return actor
    }

    fun getActor(id: String): Actor =
        actorsById[id] ?: throw IllegalArgumentException("No actor found with id $id")

    private fun onMove(actor: Actor) =
        grid.update(actor)

    fun delete(actor: Actor) {
        deletedActors.add(actor)
    }

    fun setTile(location: Location, sprite: Sprite) {
        grid.setTile(location, sprite)
    }

    fun <T : Component> actorsWith(type: KClass<out T>, action: (Actor, T) -> Unit) {
        for ((actor, component) in findActorsWith(type)) {
            action.invoke(actor, component)
        }
    }

    inline fun <reified T : Component> actorsWith(noinline action: (Actor, T) -> Unit) {
        actorsWith(T::class, action)
    }

    private fun <T : Component> findActorsWith(type: KClass<out T>): Set<Pair<Actor, T>> =
        grid.actors
            .mapNotNullTo(mutableSetOf()) { actor ->
                actor.get(type)
                    ?.let { component -> actor to component }
            }

    fun <T : Component> findActorWith(type: KClass<out T>): Pair<Actor, T>? =
        findActorsWith(type).firstOrNull()

    inline fun <reified T : Component> findActorWith(): Pair<Actor, T>? =
        findActorWith(T::class)

    fun getObjectsInArea(box: Box): Set<GameObject> {
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
