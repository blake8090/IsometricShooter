package bke.iso.game.event

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.engine.event.EventHandler
import bke.iso.engine.event.EventService
import bke.iso.engine.physics.CollisionEvent
import bke.iso.engine.world.WorldService
import bke.iso.game.Bullet

@Transient
class BulletCollisionHandler(private val eventService: EventService, private val worldService: WorldService) : EventHandler<CollisionEvent> {
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
        worldService.delete(entity)
        eventService.fire(DamageEvent(entity, otherEntity, bulletDamage))
    }
}
