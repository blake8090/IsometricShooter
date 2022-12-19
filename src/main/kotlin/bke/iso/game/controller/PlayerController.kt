package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.Engine
import bke.iso.engine.Units
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.input.Input
import bke.iso.engine.physics.MoveEvent
import bke.iso.engine.render.Renderer
import bke.iso.game.BulletType
import bke.iso.game.Player
import bke.iso.game.ShootEvent
import com.badlogic.gdx.math.Vector2

class PlayerController(
    private val input: Input,
    private val entityService: EntityService,
    private val engine: Engine,
    private val renderer: Renderer
) : Controller {
    private val playerSpeed = 5f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Player::class) { entity, _ ->
            updatePlayerEntity(entity)

            input.onAction("toggleDebug") {
                renderer.toggleDebug()
            }

            input.onAction("shoot") {
                val mousePos = renderer.unproject(input.getMousePos())
                val target = Units.toWorld(Vector2(mousePos.x, mousePos.y))
                engine.fireEvent(ShootEvent(entity, target, BulletType.PLAYER))
            }
        }
    }

    private fun updatePlayerEntity(entity: Entity) {
        val dx = input.poll("moveLeft", "moveRight")
        val dy = input.poll("moveUp", "moveDown")
        if (dx != 0f || dy != 0f) {
            engine.fireEvent(MoveEvent(entity, dx * playerSpeed, dy * playerSpeed))
        }
        val cameraPos = Units.worldToScreen(entity.x, entity.y)
        renderer.setCameraPos(cameraPos)
    }
}
