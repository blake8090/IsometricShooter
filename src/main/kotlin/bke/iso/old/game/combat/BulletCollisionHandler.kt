package bke.iso.old.game.combat

import bke.iso.old.engine.entity.Entity
import bke.iso.old.engine.log
import bke.iso.old.engine.event.EventHandler
import bke.iso.old.engine.event.EventService
import bke.iso.old.engine.physics.CollisionEvent
import bke.iso.old.engine.world.WorldService
import bke.iso.old.game.entity.Bullet
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
        val other = event.collision.obj

        log.trace("bullet collided")

        if (other is Entity) {
            // bullets should not collide with the shooter or other bullets
            if (entity.id == other.id || bullet.shooterId == other.id || other.has<Bullet>()) {
                return
            }
            eventService.fire(DamageEvent(entity, other, BULLET_DAMAGE))
        }

        worldService.delete(entity)
    }
}
