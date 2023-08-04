package bke.iso.v2.game.actor

import bke.iso.v2.engine.System
import bke.iso.v2.engine.input.Input
import bke.iso.v2.engine.physics.Velocity
import bke.iso.v2.engine.render.Renderer
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.Component
import bke.iso.v2.engine.world.World
import bke.iso.v2.game.Combat
import com.badlogic.gdx.math.Vector3

class Player : Component()

class PlayerSystem(
    private val input: Input,
    private val world: World,
    private val renderer: Renderer,
    private val combat: Combat
) : System {

    private val walkSpeed = 5f
    private val runSpeed = 10f
    private val flySpeed = 4f

    override fun update(deltaTime: Float) {
        world.actorsWith<Player> { actor, _ ->
            updatePlayer(actor)

            input.onAction("toggleDebug") {
                renderer.toggleDebug()
            }

            input.onAction("shoot") {
                combat.shoot(actor, renderer.getCursorPos(), BulletType.PLAYER)
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
        val movement = Vector3(
            input.poll("moveLeft", "moveRight"),
            input.poll("moveUp", "moveDown"),
            input.poll("flyUp", "flyDown")
        )

        val horizontalSpeed =
            if (input.poll("run") != 0f) {
                runSpeed
            } else {
                walkSpeed
            }

        val velocity = actor.components.getOrPut(Velocity())
        velocity.delta.set(movement)
        velocity.speed.set(horizontalSpeed, horizontalSpeed, flySpeed)

        renderer.setCameraPos(actor.pos)
    }
}
