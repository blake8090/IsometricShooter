package bke.iso.old.game.entity

import bke.iso.old.engine.entity.Component
import bke.iso.old.engine.system.System
import bke.iso.old.engine.world.WorldService

data class MovingPlatform(
    val speed: Float = 2f,
    val maxZ: Float = 2f,
    val minZ: Float = 0f,
    val pauseSeconds: Float = 1f
) : Component() {

    var state: State = State.UP

    enum class State {
        UP,
        DOWN
    }
}

class MovingPlatformSystem(private val worldService: WorldService) : System {
    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(MovingPlatform::class) { entity, movingPlatform ->
            when (movingPlatform.state) {
                MovingPlatform.State.UP -> {
                    entity.z += movingPlatform.speed * deltaTime
                    if (entity.z >= movingPlatform.maxZ) {
                        entity.z = movingPlatform.maxZ
                        movingPlatform.state = MovingPlatform.State.DOWN
                    }
                }

                MovingPlatform.State.DOWN -> {
                    entity.z -= movingPlatform.speed * deltaTime
                    if (entity.z <= movingPlatform.minZ) {
                        entity.z = movingPlatform.minZ
                        movingPlatform.state = MovingPlatform.State.UP
                    }
                }
            }
        }
    }
}
