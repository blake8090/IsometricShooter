package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.Units
import bke.iso.engine.entity.Component
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.input.Input
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.math.Vector2

class Player : Component()

class PlayerController(
    private val input: Input,
    private val entityService: EntityService,
    private val eventService: EventService,
    private val renderer: Renderer
) : Controller {
    private val speed = 5f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Player::class) { player, _ ->
            val dx = input.poll("moveLeft", "moveRight")
            val dy = input.poll("moveUp", "moveDown")
            if (dx != 0f || dy != 0f) {
                eventService.fire(MoveEvent(player, dx * speed, dy * speed))
            }
            val cameraPos = Units.worldToScreen(player.x, player.y)
            renderer.setCameraPos(cameraPos)

            input.onAction("shoot") {
                val mousePos = renderer.unproject(input.getMousePos())
                val worldPos = Units.toWorld(Vector2(mousePos.x, mousePos.y))
                val bullet = entityService.create(worldPos.x, worldPos.y)
                bullet.add(
                    Sprite("circle", 0f, 0f)
                )
            }
        }
    }
}
