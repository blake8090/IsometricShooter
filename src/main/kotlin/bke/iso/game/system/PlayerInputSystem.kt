//package bke.iso.game.system
//
//import bke.iso.engine.render.Renderer
//import bke.iso.engine.Units
//import bke.iso.engine.entity.Entities
//import bke.iso.engine.input.Input
//import bke.iso.engine.log
//import bke.iso.engine.system.System
//import bke.iso.engine.system.Velocity
//import bke.iso.game.EntityFactory
//import bke.iso.game.PlayerComponent
//import com.badlogic.gdx.math.Vector2
//
//class PlayerInputSystem(
//    private val entities: Entities,
//    private val input: Input,
//    private val renderer: Renderer,
//    private val entityFactory: EntityFactory
//) : System {
//    private val walkSpeed = 3f
//    private val runSpeed = 5f
//
//    override fun update(deltaTime: Float) {
//        val player = entities.components
//            .getIdsWith(PlayerComponent::class)
//            .firstOrNull()
//            ?.let(entities::getEntity)
//            ?: return
//
//        input.onAction("toggleDebug") {
//            renderer.toggleDebug()
//        }
//
//        input.onAction("shoot") {
//            log.debug("shoot action")
//            val screenPos = renderer.unproject(input.getMousePos())
//            val worldPos = Units.screenToWorld(Vector2(screenPos.x, screenPos.y))
//            entityFactory.createBullet(
//                player.getPos(),
//                player.id,
//                worldPos,
//                50f
//            )
//        }
//
//        movePlayer()
//    }
//
//    private fun movePlayer() {
//        val dx = input.poll("moveLeft", "moveRight")
//        val dy = input.poll("moveUp", "moveDown")
//
//        var speed = walkSpeed
//        input.onAction("run") {
//            speed = runSpeed
//        }
//
//        entities.withComponent(PlayerComponent::class) { player, _ ->
//            player.addComponent(Velocity(dx * speed, dy * speed))
//        }
//    }
//}
