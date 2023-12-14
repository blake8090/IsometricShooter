package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.input.Input
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import bke.iso.game.weapon.RangedWeaponOffset
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

private const val PLAYER_JUMP_FORCE = 6f
private const val CONTROLLER_DEADZONE = 0.1f

class PlayerSystem(
    private val input: Input,
    private val world: World,
    private val renderer: Renderer
) : System {

    private val log = KotlinLogging.logger {}

    private val walkSpeed = 5f
    private val runSpeed = 10f

    override fun update(deltaTime: Float) {
        world.actors.each<Player> { actor, player ->
            updatePlayer(actor, player)

            input.onAction("toggleDebug") {
                renderer.debug.toggle()
            }
        }
    }

    private fun updatePlayer(playerActor: Actor, player: Player) {
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

        val body = checkNotNull(playerActor.get<PhysicsBody>()) {
            "Expected $playerActor to have a PhysicsBody"
        }
        body.forces.add(movement)
        input.onAction("jump") {
            body.velocity.z = PLAYER_JUMP_FORCE
        }

        if (player.state == PlayerState.NONE) {
            switchState(playerActor, player, PlayerState.STAND)
        }

        input.onAction("crouch") {
            if (player.state == PlayerState.STAND) {
                switchState(playerActor, player, PlayerState.CROUCH)
            } else if (player.state == PlayerState.CROUCH) {
                switchState(playerActor, player, PlayerState.STAND)
            }
        }

        renderer.setCameraPos(playerActor.pos)
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
                        size = Vector3(0.4f, 0.4f, 1.4f),
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
