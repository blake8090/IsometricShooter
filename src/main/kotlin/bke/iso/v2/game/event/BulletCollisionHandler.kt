package bke.iso.v2.game.event

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.v2.engine.event.EventHandler
import bke.iso.v2.engine.event.EventService
import bke.iso.v2.engine.physics.CollisionEvent
import bke.iso.v2.game.Bullet

@Transient
class BulletCollisionHandler(private val eventService: EventService) : EventHandler<CollisionEvent> {
    override val type = CollisionEvent::class

    private val bulletDamage = 1f

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
        eventService.fire(DamageEvent(entity, otherEntity, bulletDamage))
    }
}
