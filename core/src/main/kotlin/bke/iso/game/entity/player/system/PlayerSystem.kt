package bke.iso.game.entity.player.system

import bke.iso.engine.asset.config.Configs
import bke.iso.engine.state.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.Collisions
import bke.iso.engine.input.Input
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import bke.iso.game.entity.Inventory
import bke.iso.game.entity.Medkit
import bke.iso.game.entity.player.Player
import bke.iso.game.entity.player.PlayerConfig
import bke.iso.game.entity.player.PlayerState
import bke.iso.game.combat.CombatModule
import bke.iso.game.weapon.system.RangedWeaponOffset
import bke.iso.game.weapon.system.WeaponPickup
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

private const val CONTROLLER_DEADZONE = 0.1f

class PlayerSystem(
    private val input: Input,
    private val world: World,
    private val renderer: Renderer,
    private val collisions: Collisions,
    private val combatModule: CombatModule,
    private val weaponsModule: WeaponsModule,
    private val configs: Configs
) : System {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        world.entities.each<Player> { entity, player ->
            updatePlayer(entity, player)

            input.onAction("toggleDebug") {
                renderer.debug.toggle()
            }
        }
    }

    private fun updatePlayer(playerEntity: Entity, player: Player) {
        input.onAction("crouch") {
            if (player.state == PlayerState.STAND) {
                setCrouchState(playerEntity, player)
            } else if (player.state == PlayerState.CROUCH) {
                setStandState(playerEntity, player)
            }
        }

        if (player.state == PlayerState.NONE) {
            setStandState(playerEntity, player)
        }

        val direction = input.pollAxes(actionX = "moveX", actionY = "moveY", CONTROLLER_DEADZONE)
        move(playerEntity, player, direction)

        renderer.setCameraPos(playerEntity.pos)

        for (collision in collisions.getCollisions(playerEntity)) {
            handleCollision(playerEntity, collision)
        }

        input.onAction("useMedkit") {
            useMedkit(playerEntity)
        }
    }

    private fun move(playerEntity: Entity, player: Player, direction: Vector2) {
        val playerConfig = configs.get<PlayerConfig>("player.cfg")

        val horizontalSpeed = getHorizontalSpeed(player, playerConfig)
        val movement = Vector3(
            direction.x * horizontalSpeed,
            direction.y * horizontalSpeed,
            0f
        )

        // makes up actually move up
        if (input.isUsingController()) {
            movement.rotate(Vector3.Z, 45f)
        }

        val body = checkNotNull(playerEntity.get<PhysicsBody>()) {
            "Expected $playerEntity to have a PhysicsBody"
        }
        body.forces.add(movement)
        input.onAction("jump") {
            body.velocity.z = playerConfig.jumpForce
        }
    }

    private fun getHorizontalSpeed(player: Player, playerConfig: PlayerConfig): Float {
        var speed = playerConfig.baseMovementSpeed

        if (player.state == PlayerState.CROUCH) {
            speed *= playerConfig.crouchSpeedModifier
        } else if (input.poll("run") == 1f) {
            speed *= playerConfig.runSpeedModifier
        }

        return speed
    }

    private fun handleCollision(playerEntity: Entity, collision: Collision) {
        val otherEntity = collision.entity

        if (otherEntity.has<Medkit>()) {
            val inventory = playerEntity.getOrAdd(Inventory())
            inventory.numMedkits++
            log.debug { "Picked up medkit. Player now has ${inventory.numMedkits} medkits" }
            world.delete(otherEntity)
        } else if (otherEntity.has<WeaponPickup>()) {
            log.debug { "Picking up weapon" }
            val weaponName = otherEntity.get<WeaponPickup>()!!.name
            weaponsModule.equip(playerEntity, weaponName)
            world.delete(otherEntity)
        }
    }

    private fun useMedkit(playerEntity: Entity) {
        val inventory = playerEntity.get<Inventory>()

        if (inventory == null || inventory.numMedkits <= 0) {
            log.debug { "No medkits" }
            return
        }

        combatModule.heal(playerEntity)
        inventory.numMedkits--
        log.debug { "used medkit" }
    }

    private fun setStandState(playerEntity: Entity, player: Player) {
        player.state = PlayerState.STAND
        playerEntity.add(
            Sprite(
                texture = "character-stand.png",
                offsetX = 16.0f
            )
        )
        playerEntity.add(
            Collider(
                size = Vector3(0.4f, 0.4f, 1.5f),
                offset = Vector3(-0.2f, -0.2f, 0.0f)
            )
        )
        playerEntity.add(RangedWeaponOffset(0f, 0f, 0.95f))
    }

    private fun setCrouchState(playerEntity: Entity, player: Player) {
        player.state = PlayerState.CROUCH
        playerEntity.add(
            Sprite(
                texture = "character-crouch.png",
                offsetX = 18.0f,
            )
        )
        playerEntity.add(
            Collider(
                size = Vector3(0.4f, 0.4f, 1.0f),
                offset = Vector3(-0.2f, -0.2f, 0.0f)
            )
        )
        playerEntity.add(RangedWeaponOffset(0f, 0f, 0.7f))
    }
}
