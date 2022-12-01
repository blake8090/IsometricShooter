package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.Units
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.input.Input
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.game.Bullet
import bke.iso.game.Player
import com.badlogic.gdx.math.Vector2

class PlayerController(
    private val input: Input,
    private val entityService: EntityService,
    private val eventService: EventService,
    private val renderer: Renderer
) : Controller {
    private val playerSpeed = 5f
    private val bulletSpeed = 5f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Player::class) { entity, _ ->
            updatePlayerEntity(entity)

            input.onAction("shoot") {
                val mousePos = renderer.unproject(input.getMousePos())
                val target = Units.toWorld(Vector2(mousePos.x, mousePos.y))
                shoot(entity, target)
            }
        }
    }

    private fun updatePlayerEntity(entity: Entity) {
        val dx = input.poll("moveLeft", "moveRight")
        val dy = input.poll("moveUp", "moveDown")
        if (dx != 0f || dy != 0f) {
            eventService.fire(MoveEvent(entity, dx * playerSpeed, dy * playerSpeed))
        }
        val cameraPos = Units.worldToScreen(entity.x, entity.y)
        renderer.setCameraPos(cameraPos)
    }

    // TODO: this should be an event instead!
    private fun shoot(playerEntity: Entity, target: Vector2) {
        val pos = Vector2(playerEntity.x, playerEntity.y)
        val direction = Vector2(target).sub(pos).nor()

        val bullet = entityService.create(playerEntity.x, playerEntity.y)
        bullet.vx = direction.x * bulletSpeed
        bullet.vy = direction.y * bulletSpeed
        bullet.add(
            Bullet(
                playerEntity.id,
                bulletSpeed,
                target
            ),
            Sprite("circle", 8f, 16f),
            Collision(
                Bounds(0.25f, 0.25f, 0f, 0f),
                false
            )
        )
    }
}
