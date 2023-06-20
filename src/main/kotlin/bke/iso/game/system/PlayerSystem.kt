package bke.iso.game.system

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventService
import bke.iso.engine.input.InputService
import bke.iso.engine.log
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.physics.collision.CollisionServiceV2
import bke.iso.engine.render.RenderService
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService
import bke.iso.game.EntityFactory
import bke.iso.game.Player
import bke.iso.game.event.BulletType
import bke.iso.game.event.ShootEvent
import com.badlogic.gdx.math.Vector3
import kotlin.math.max

class PlayerSystem(
    private val worldService: WorldService,
    private val inputService: InputService,
    private val eventService: EventService,
    private val renderService: RenderService,
    private val entityFactory: EntityFactory,
    private val collisionServiceV2: CollisionServiceV2
) : System {

    private val walkSpeed = 5f
    private val runSpeed = 10f
    private val flySpeed = 4f

    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Player::class) { entity, _ ->
            updatePlayerEntity(entity, deltaTime)

            inputService.onAction("toggleDebug") {
                renderService.toggleDebugMode()
            }

            inputService.onAction("shoot") {
                val mousePos = inputService.getMousePos()
                val target = renderService.unproject(mousePos)
                eventService.fire(ShootEvent(entity, target, BulletType.PLAYER))
            }

            inputService.onAction("placeBouncyBall") {
                val mousePos = inputService.getMousePos()
                val target = renderService.unproject(mousePos)
                entityFactory.createBouncyBall(target.x, target.y, 0f)
            }

            inputService.onAction("checkCollisions") {
                val predictedCollisions = collisionServiceV2.findProjectedCollisions(entity, 0f, 0f, 0f)
                if (predictedCollisions != null) {
                    log.debug("found ${predictedCollisions.collisions.size} collisions in projected area")
                }
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
