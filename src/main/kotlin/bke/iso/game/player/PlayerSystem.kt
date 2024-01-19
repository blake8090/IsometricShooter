package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.input.Input
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import bke.iso.game.weapon.RangedWeaponOffset
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

private const val PLAYER_JUMP_FORCE = 5f
private const val CONTROLLER_DEADZONE = 0.1f

private const val BASE_MOVEMENT_SPEED = 3.5f
private const val CROUCH_SPEED_MODIFIER = 0.6f
private const val RUN_SPEED_MODIFIER = 1.6f

class PlayerSystem(
    private val input: Input,
    private val world: World,
    private val renderer: Renderer
) : System {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        world.actors.each<Player> { actor, player ->
            updatePlayer(actor, player)

            input.onAction("toggleDebug") {
                renderer.debug.toggle()
            }
        }
    }

    private fun updatePlayer(playerActor: Actor, player: Player) {
        input.onAction("crouch") {
            if (player.state == PlayerState.STAND) {
                switchState(playerActor, player, PlayerState.CROUCH)
            } else if (player.state == PlayerState.CROUCH) {
                switchState(playerActor, player, PlayerState.STAND)
            }
        }

        if (player.state == PlayerState.NONE) {
            switchState(playerActor, player, PlayerState.STAND)
        }

        val direction = input.pollAxes(actionX = "moveX", actionY = "moveY", CONTROLLER_DEADZONE)
        move(playerActor, player, direction)

        renderer.setCameraPos(playerActor.pos)
    }

    private fun move(playerActor: Actor, player: Player, direction: Vector2) {
        val horizontalSpeed = getHorizontalSpeed(player)
        val movement = Vector3(
            direction.x * horizontalSpeed,
            direction.y * horizontalSpeed,
            0f
        )

        val body = checkNotNull(playerActor.get<PhysicsBody>()) {
            "Expected $playerActor to have a PhysicsBody"
        }
        body.forces.add(movement)
        input.onAction("jump") {
            body.velocity.z = PLAYER_JUMP_FORCE
        }
    }

    private fun getHorizontalSpeed(player: Player): Float {
        var speed = BASE_MOVEMENT_SPEED

        if (player.state == PlayerState.CROUCH) {
            speed *= CROUCH_SPEED_MODIFIER
        } else if (input.poll("run") == 1f) {
            speed *= RUN_SPEED_MODIFIER
        }

        return speed
    }

    private fun switchState(playerActor: Actor, player: Player, state: PlayerState) {
        log.debug { "player state set from ${player.state} to $state at z: ${playerActor.z}" }
        when (state) {
            PlayerState.STAND -> {
                player.state = PlayerState.STAND
                playerActor.add(
                    Sprite(
                        texture = "character-stand.png",
                        offsetX = 16.0f
                    )
                )
                playerActor.add(
                    Collider(
                        size = Vector3(0.4f, 0.4f, 1.5f),
                        offset = Vector3(-0.2f, -0.2f, 0.0f)
                    )
                )
                playerActor.add(RangedWeaponOffset(0f, 0f, 1.13f))
            }

            PlayerState.CROUCH -> {
                player.state = PlayerState.CROUCH
                playerActor.add(
                    Sprite(
                        texture = "character-crouch.png",
                        offsetX = 18.0f,
                    )
                )
                playerActor.add(
                    Collider(
                        size = Vector3(0.4f, 0.4f, 1.0f),
                        offset = Vector3(-0.2f, -0.2f, 0.0f)
                    )
                )
                playerActor.add(RangedWeaponOffset(0f, 0f, 0.7f))
            }

            else -> {}
        }
    }
}
