package bke.iso.game.event

import bke.iso.engine.Engine
import bke.iso.engine.event.EventHandler
import bke.iso.engine.log
import bke.iso.engine.physics.CollisionEvent
import bke.iso.game.Bullet

class BulletCollisionHandler(private val engine: Engine) : EventHandler<CollisionEvent> {
    override val type = CollisionEvent::class

    override fun handle(event: CollisionEvent) {
        val entity = event.entity
        val bullet = entity.get<Bullet>() ?: return
        val otherEntity = event.collisionData.entity

        // bullets should not collide with the shooter or other bullets
        if (bullet.shooterId == otherEntity.id || otherEntity.has<Bullet>()) {
            return
        }

        log.trace("bullet collided")
        entity.delete()
        engine.fireEvent(DamageEvent(entity, otherEntity, 5f))
    }
}
