package bke.iso.v2.game.system

import bke.iso.service.Transient
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.event.EventService
import bke.iso.v2.engine.input.InputService
import bke.iso.v2.engine.math.toScreen
import bke.iso.v2.engine.physics.MoveEvent
import bke.iso.v2.engine.render.RenderService
import bke.iso.v2.engine.system.System
import bke.iso.v2.game.Player

@Transient
class PlayerSystem(
    private val entityService: EntityService,
    private val inputService: InputService,
    private val eventService: EventService,
    private val renderService: RenderService
) : System {
    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Player::class) { entity, _ ->
            updatePlayerEntity(entity, deltaTime)

            inputService.onAction("toggleDebug") {
                renderService.toggleDebugMode()
            }

//            inputService.onAction("shoot") {
//                val mousePos = renderService.unproject(input.getMousePos())
//                val target = Units.toWorld(Vector2(mousePos.x, mousePos.y))
//                engine.fireEvent(ShootEvent(entity, target, BulletType.PLAYER))
//            }
        }
    }

    private fun updatePlayerEntity(entity: Entity, deltaTime: Float) {
        val dx = inputService.poll("moveLeft", "moveRight")
        val dy = inputService.poll("moveUp", "moveDown")
        if (dx != 0f || dy != 0f) {
            val speed =
                if (inputService.poll("run") != 0f) {
                    20f
                } else {
                    10f
                }
            eventService.fire(MoveEvent(entity, dx, dy, speed, deltaTime))
        }
        renderService.setCameraPos(toScreen(entity.x, entity.y))
    }
}
