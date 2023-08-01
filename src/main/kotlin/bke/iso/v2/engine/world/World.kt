package bke.iso.v2.engine.world

import bke.iso.engine.entity.Component
import bke.iso.engine.math.Location
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import java.util.UUID

class World(game: Game) : Module(game) {

    private val grid = Grid()

    val objects: Set<GameObject>
        get() = grid.objects

    fun newActor(
        x: Float, y: Float, z: Float,
        vararg components: Component,
        id: UUID = UUID.randomUUID()
    ) {
        val actor = Actor(id, x, y, z, this::onMove)
        components.forEach { component -> actor.components[component::class] = component }
        grid.add(actor)
    }

    fun setTile(location: Location, texture: String, solid: Boolean = false) {
        val tile = Tile(UUID.randomUUID(), location, texture, solid, ::onMove)
        grid.add(tile)
    }

    private fun onMove(gameObject: GameObject) =
        grid.move(gameObject, Location(gameObject.pos))
}
