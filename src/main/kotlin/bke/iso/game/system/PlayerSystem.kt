package bke.iso.game.system

import bke.iso.service.Transient
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.input.InputService
import bke.iso.engine.math.toScreen
import bke.iso.engine.math.toWorld
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.render.RenderService
import bke.iso.engine.system.System
import bke.iso.game.Player
import bke.iso.game.event.BulletType
import bke.iso.game.event.ShootEvent
import com.badlogic.gdx.math.Vector3

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

            inputService.onAction("shoot") {
                val mousePos = inputService.getMousePos()
                val target = toWorld(renderService.unproject(Vector3(mousePos, 0f)))
                eventService.fire(ShootEvent(entity, target, BulletType.PLAYER))
            }
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