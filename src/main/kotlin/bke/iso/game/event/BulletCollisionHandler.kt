package bke.iso.game.event

import bke.iso.engine.entity.Entity
import bke.iso.engine.log
import bke.iso.engine.event.EventHandler
import bke.iso.engine.event.EventService
import bke.iso.engine.physics.collision.CollisionEvent
import bke.iso.engine.world.WorldService
import bke.iso.game.Bullet
import kotlin.reflect.safeCast

private const val BULLET_DAMAGE = 1f

class BulletCollisionHandler(
    private val eventService: EventService,
    private val worldService: WorldService
) : EventHandler<CollisionEvent> {

    override val type = CollisionEvent::class

    override fun handle(event: CollisionEvent) {
        val entity = Entity::class.safeCast(event.obj) ?: return
        val bullet = entity.get<Bullet>() ?: return
        val otherEntity = Entity::class.safeCast(event.collision.obj) ?: return

        log.trace("handling collision event")

        // bullets should not collide with the shooter or other bullets
        if (bullet.shooterId == otherEntity.id || otherEntity.has<Bullet>()) {
            return
        }

        log.trace("bullet collided")
        worldService.delete(entity)
        eventService.fire(DamageEvent(entity, otherEntity, BULLET_DAMAGE))
    }
}
