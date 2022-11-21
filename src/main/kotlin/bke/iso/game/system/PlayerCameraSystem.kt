package bke.iso.game.system

import bke.iso.engine.Renderer
import bke.iso.engine.entity.Entities
import bke.iso.engine.system.System
import bke.iso.game.PlayerComponent

class PlayerCameraSystem(
    private val entities: Entities,
    private val renderer: Renderer
) : System {
    override fun update(deltaTime: Float) {
        entities.withComponent(PlayerComponent::class) { player, _ ->
            renderer.setCameraPos(player.getPos())
        }
    }
}
