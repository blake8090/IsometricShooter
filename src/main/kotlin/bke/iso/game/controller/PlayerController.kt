package bke.iso.game.controller

import bke.iso.engine.*
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.input.Input
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.render.RenderService
import bke.iso.game.event.BulletType
import bke.iso.game.Player
import bke.iso.game.event.ShootEvent
import com.badlogic.gdx.math.Vector2

class PlayerController(
    private val input: Input,
    private val entityService: EntityService,
    private val engine: Engine,
    private val renderService: RenderService
) : Controller {
    private val playerWalkSpeed = 5f
    private val playerRunSpeed = 10f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Player::class) { entity, _ ->
            updatePlayerEntity(entity)

            input.onAction("toggleDebug") {
                renderService.toggleDebug()
            }

            input.onAction("shoot") {
                val mousePos = renderService.unproject(input.getMousePos())
                val target = Units.toWorld(Vector2(mousePos.x, mousePos.y))
                engine.fireEvent(ShootEvent(entity, target, BulletType.PLAYER))
            }
        }
    }

    private fun updatePlayerEntity(entity: Entity) {
        val dx = input.poll("moveLeft", "moveRight")
        val dy = input.poll("moveUp", "moveDown")
        if (dx != 0f || dy != 0f) {
            val speed =
                if (input.poll("run") != 0f) {
                    playerRunSpeed
                } else {
                    playerWalkSpeed
                }
            engine.fireEvent(MoveEvent(entity, dx, dy, speed))
        }
        renderService.setCameraPos(toScreen(entity.x, entity.y))
    }
}
