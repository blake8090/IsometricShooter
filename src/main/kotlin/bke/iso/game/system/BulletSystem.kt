//package bke.iso.game.system
//
//import bke.iso.engine.entity.Component
//import bke.iso.engine.entity.Entities
//import bke.iso.engine.entity.Entity
//import bke.iso.engine.log
//import bke.iso.engine.system.CollisionEvents
//import bke.iso.engine.system.System
//import bke.iso.engine.system.Velocity
//import com.badlogic.gdx.math.Vector2
//import java.util.UUID
//
//data class BulletComponent(
//    val shooterId: UUID,
//    val target: Vector2,
//    val speed: Float
//) : Component()
//
//class BulletSystem(private val entities: Entities) : System {
//    override fun update(deltaTime: Float) {
//        entities.withComponent(BulletComponent::class) { entity, bulletComponent ->
//            if (!entity.hasComponent<Velocity>()) {
//                setupBullet(entity, bulletComponent)
//            }
//            checkCollisions(entity, bulletComponent)
//        }
//    }
//
//    private fun setupBullet(entity: Entity, bulletComponent: BulletComponent) {
//        val pos = entity.getPos()
//        val direction = Vector2(bulletComponent.target)
//            .sub(pos)
//            .nor()
//        val velocity = Velocity(
//            direction.x * bulletComponent.speed,
//            direction.y * bulletComponent.speed
//        )
//        entity.addComponent(velocity)
//    }
//
//    private fun checkCollisions(entity: Entity, bulletComponent: BulletComponent) {
//        val collisionEvents = entity.getComponent<CollisionEvents>() ?: return
//        val event = collisionEvents.events
//            .firstOrNull { event -> event.id != bulletComponent.shooterId }
//            ?: return
//
//        log.debug("Bullet collided with entity: ${event.id}")
//        entities.delete(entity.id)
//    }
//}
