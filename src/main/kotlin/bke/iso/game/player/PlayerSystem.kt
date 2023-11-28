package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import bke.iso.game.combat.Combat
import bke.iso.game.actor.BulletType
import com.badlogic.gdx.math.Vector3

private const val PLAYER_JUMP_FORCE = 6f
private const val CONTROLLER_DEADZONE = 0.1f

class PlayerSystem(
    private val input: Input,
    private val world: World,
    private val renderer: Renderer,
    private val combat: Combat
) : System {

    private val walkSpeed = 5f
    private val runSpeed = 10f

    override fun update(deltaTime: Float) {
        world.actors.each<Player> { actor, _ ->
            updatePlayer(actor)

            input.onAction("toggleDebug") {
                renderer.debug.toggle()
            }

            input.onAction("shoot") {
                val target = toWorld(renderer.getPointerPos())
                target.z = actor.z
                combat.shoot(actor, target, BulletType.PLAYER)
            }
//
//            inputService.onAction("placeBouncyBall") {
//                val mousePos = inputService.getMousePos()
//                val target = renderService.unproject(mousePos)
//                entityFactory.createBouncyBall(target.x, target.y, 0f)
//            }
//
//            inputService.onAction("checkCollisions") {
//                checkPlayerCollisions(entity)
//            }
        }
    }

    private fun updatePlayer(actor: Actor) {
        val horizontalSpeed =
            if (input.poll("run") != 0f) {
                runSpeed
            } else {
                walkSpeed
            }

        val direction = input.pollAxes(actionX = "moveX", actionY = "moveY", CONTROLLER_DEADZONE)
        val movement = Vector3(
            direction.x * horizontalSpeed,
            direction.y * horizontalSpeed,
            0f
        )

        val body = checkNotNull(actor.get<PhysicsBody>()) {
            "Expected $actor to have a PhysicsBody"
        }
        body.forces.add(movement)
        input.onAction("jump") {
            body.velocity.z = PLAYER_JUMP_FORCE
        }

        renderer.setCameraPos(actor.pos)
    }
}
