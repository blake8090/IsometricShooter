package bke.iso.v2.engine.world

import bke.iso.engine.entity.Component
import bke.iso.engine.math.Location
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import java.util.UUID
import kotlin.reflect.KClass

class World(game: Game) : Module(game) {

    private val grid = Grid()

    val objects: Set<GameObject>
        get() = grid.objects

    fun newActor(
        x: Float, y: Float, z: Float,
        vararg components: Component,
        id: UUID = UUID.randomUUID()
    ): Actor {
        val actor = Actor(id, x, y, z, this::onMove)
        components.forEach { component -> actor.components[component::class] = component }
        grid.add(actor)
        return actor
    }

    fun setTile(location: Location, texture: String, solid: Boolean = false) {
        val tile = Tile(UUID.randomUUID(), location, texture, solid, ::onMove)
        grid.add(tile)
    }

    private fun onMove(gameObject: GameObject) =
        grid.move(gameObject, Location(gameObject.pos))

    fun <T : Component> actorsWith(type: KClass<out T>, action: (Actor, T) -> Unit) {
        for (actor in grid.objects.filterIsInstance<Actor>()) {
            val component = actor.components[type] ?: continue
            action.invoke(actor, component)
        }
    }

    inline fun <reified T : Component> actorsWith(noinline action: (Actor, T) -> Unit) =
        actorsWith(T::class, action)
}
