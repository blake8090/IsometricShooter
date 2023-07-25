package bke.iso.game.system

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.EventService
import bke.iso.engine.input.InputService
import bke.iso.engine.physics.Velocity
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
) : System {

    private val walkSpeed = 5f
    private val runSpeed = 10f
    private val flySpeed = 4f

    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Player::class) { entity, _ ->
            updatePlayer(entity)

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
        }
    }

    private fun updatePlayer(entity: Entity) {
        val movement = Vector3(
            inputService.poll("moveLeft", "moveRight"),
            inputService.poll("moveUp", "moveDown"),
            inputService.poll("flyUp", "flyDown")
        )

        val horizontalSpeed =
            if (inputService.poll("run") != 0f) {
                runSpeed
            } else {
                walkSpeed
            }

        val velocity = getOrAddVelocity(entity)
        velocity.delta.set(movement)
        velocity.speed.set(horizontalSpeed, horizontalSpeed, flySpeed)

        renderService.setCameraPos(Vector3(entity.x, entity.y, entity.z))
    }

    private fun getOrAddVelocity(entity: Entity): Velocity {
        val velocity = entity.get<Velocity>()
        if (velocity != null) {
            return velocity
        }
        val newVelocity = Velocity()
        entity.add(newVelocity)
        return newVelocity
    }
}
