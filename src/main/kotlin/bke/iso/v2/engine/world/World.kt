package bke.iso.v2.engine.world

import bke.iso.engine.entity.Component
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.Module
import java.util.UUID


class World(game: Game) : Module(game) {
    private val actors = mutableSetOf<Actor>()

    fun newActor(
        x: Float,
        y: Float,
        z: Float,
        vararg components: Component
    ) {
        val actor = Actor(UUID.randomUUID(), x, y, z, this::onMove)
        for (component in components) {
            actor.components[component::class] = component
        }
        actors.add(actor)
    }

    private fun onMove(gameObject: GameObject) {}

    fun getActors(): Set<Actor> =
        actors
}
