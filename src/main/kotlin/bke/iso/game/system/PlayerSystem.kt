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

class PlayerSystem(
    private val worldService: WorldService,
    private val inputService: InputService,
    private val eventService: EventService,
    private val renderService: RenderService,
    private val entityFactory: EntityFactory,
    private val collisionService: CollisionServiceV2
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
                val predictedCollisions = collisionService.predictEntityCollisions(entity, 0f, 0f, 0f)
                if (predictedCollisions != null) {
                    log.debug("found ${predictedCollisions.collisions.size} collisions in projected area")
                }
            }
        }
    }

    private fun updatePlayerEntity(entity: Entity, deltaTime: Float) {
        val velocity = Vector3(
            inputService.poll("moveLeft", "moveRight"),
            inputService.poll("moveUp", "moveDown"),
            inputService.poll("flyUp", "flyDown")
        )

        if (!velocity.isZero) {
            move(entity, velocity, deltaTime)
        }

        renderService.setCameraPos(Vector3(entity.x, entity.y, entity.z))
    }

    private fun move(entity: Entity, velocity: Vector3, deltaTime: Float) {
        val horizontalSpeed =
            if (inputService.poll("run") != 0f) {
                runSpeed
            } else {
                walkSpeed
            }

        val event = MoveEvent(
            entity,
            velocity,
            Vector3(horizontalSpeed, horizontalSpeed, flySpeed),
            deltaTime
        )
        eventService.fire(event)
    }
}
