package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.input.Input
import bke.iso.engine.math.Location
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collider
import bke.iso.engine.physics.Velocity
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.World
import bke.iso.game.Combat
import bke.iso.game.Health
import bke.iso.game.HealthBar
import com.badlogic.gdx.math.Vector3

const val PLAYER_MAX_HEALTH = 5f

class Player : Component()

class PlayerSystem(
    private val input: Input,
    private val world: World,
    private val renderer: Renderer,
    private val combat: Combat
) : System() {

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
                combat.shoot(actor, renderer.cursor.worldPos, BulletType.PLAYER)
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
            input.poll("moveX"),
            input.poll("moveY"),
            input.poll("fly")
        )

        val magnitude = movement.len()
        // by normalizing the movement vector, we ensure that the player doesn't move faster diagonally
        movement.nor()
        // when using a controller, reapply the magnitude to support precise movement
        if (input.isUsingController()) {
            movement.scl(magnitude)
        }

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

fun World.createPlayer(location: Location) =
    newActor(
        location.x.toFloat(), location.y.toFloat(), location.z.toFloat(),
        Sprite("player", 32f, 0f),
        Player(),
        Collider(
            Bounds(Vector3(0.5f, 0.5f, 1.6f)),
            false
        ),
        Health(PLAYER_MAX_HEALTH),
        HealthBar(18f, -64f)
    )
