//package bke.iso.game
//
//import bke.iso.app.service.Service
//import bke.iso.engine.entity.Component
//import bke.iso.engine.entity.Entities
//import bke.iso.engine.entity.Sprite
//import bke.iso.engine.system.Collision
//import bke.iso.engine.system.CollisionBounds
//import bke.iso.game.system.BulletComponent
//import com.badlogic.gdx.math.Vector2
//import java.util.*
//
//class PlayerComponent : Component()
//
//@Service
//class EntityFactory(private val entities: Entities) {
//    fun createWall(x: Float, y: Float) {
//        entities.create(x, y)
//            .addComponent(Sprite("wall3", Vector2(0f, 16f)))
//            .addComponent(
//                Collision(
//                    CollisionBounds(
//                        1f,
//                        1f
//                    ),
//                    true
//                )
//            )
//    }
//
//    fun createPlayer() =
//        entities.create(1f, 0f)
//            .addComponent(PlayerComponent())
//            .addComponent(
//                Sprite(
//                    "player",
//                    Vector2(32f, 0f)
//                )
//            )
//            .addComponent(
//                Collision(
//                    CollisionBounds(
//                        0.5f,
//                        0.5f,
//                        Vector2(-0.25f, -0.25f)
//                    )
//                )
//            )
//
//    fun createBullet(
//        pos: Vector2,
//        shooter: UUID,
//        target: Vector2,
//        speed: Float
//    ) =
//        entities.create(pos.x, pos.y)
//            .addComponents(
//                BulletComponent(shooter, target, speed),
//                Sprite("circle", Vector2(16f, 16f)),
//                Collision(
//                    CollisionBounds(
//                        0.25f,
//                        0.25f
//                    )
//                )
//            )
//}
