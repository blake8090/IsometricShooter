package bke.iso.game.system

import bke.iso.service.Transient
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.input.InputService
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.render.RenderService
import bke.iso.engine.system.System
import bke.iso.game.Player
import bke.iso.game.event.BulletType
import bke.iso.game.event.ShootEvent
import com.badlogic.gdx.math.Vector3
import kotlin.math.max

@Transient
class PlayerSystem(
    private val entityService: EntityService,
    private val inputService: InputService,
    private val eventService: EventService,
    private val renderService: RenderService
) : System {

    private val walkSpeed = 5f
    private val runSpeed = 10f
    private val flySpeed = 3f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Player::class) { entity, _ ->
            updatePlayerEntity(entity, deltaTime)

            inputService.onAction("toggleDebug") {
                renderService.toggleDebugMode()
            }

            inputService.onAction("shoot") {
                val mousePos = inputService.getMousePos()
                val target = renderService.unproject(mousePos)
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
                    runSpeed
                } else {
                    walkSpeed
                }
            eventService.fire(MoveEvent(entity, dx, dy, speed, deltaTime))
        }
        renderService.setCameraPos(Vector3(entity.x, entity.y, entity.z))

        val dz = inputService.poll("flyUp", "flyDown")
        if (dz != 0f) {
            val newZ = entity.z + (dz * flySpeed * deltaTime)
            entity.z = max(newZ, 0f)
        }
    }
}
