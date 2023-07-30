package bke.iso.game.combat

import bke.iso.engine.entity.Entity
import bke.iso.engine.log
import bke.iso.engine.event.EventHandler
import bke.iso.engine.event.EventService
import bke.iso.engine.physics.CollisionEvent
import bke.iso.engine.world.WorldService
import bke.iso.game.entity.Bullet
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
