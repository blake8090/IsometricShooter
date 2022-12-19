package bke.iso.game

import bke.iso.engine.event.EventHandler
import bke.iso.engine.log
import bke.iso.engine.physics.CollisionEvent

class BulletCollisionHandler : EventHandler<CollisionEvent> {
    override val type = CollisionEvent::class

    override fun handle(event: CollisionEvent) {
        val entity = event.entity
        val bullet = entity.get<Bullet>() ?: return
        val otherEntity = event.collisionDetails.entity

        // bullets should not collide with the shooter or other bullets
        if (bullet.shooterId == otherEntity.id || otherEntity.has<Bullet>()) {
            return
        }

        log.trace("bullet collided")
        entity.delete()
    }
}
