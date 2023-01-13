package bke.iso.v2.game.event

import bke.iso.service.Transient
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.event.Event
import bke.iso.v2.engine.event.EventHandler
import bke.iso.v2.engine.physics.Bounds
import bke.iso.v2.engine.physics.Collision
import bke.iso.v2.engine.physics.Velocity
import bke.iso.v2.engine.render.Sprite
import bke.iso.v2.game.Bullet
import com.badlogic.gdx.math.Vector2

enum class BulletType {
    PLAYER,
    TURRET
}

data class ShootEvent(
    val shooter: Entity,
    val target: Vector2,
    val type: BulletType
) : Event()

@Transient
class ShootHandler(private val entityService: EntityService) : EventHandler<ShootEvent> {
    override val type = ShootEvent::class

    private val playerBulletSpeed = 30f
    private val turretBulletSpeed = 20f

    override fun handle(event: ShootEvent) {
        val shooter = event.shooter
        val target = event.target

        val pos = Vector2(shooter.x, shooter.y)
        val direction = Vector2(target).sub(pos).nor()
        val speed = when (event.type) {
            BulletType.PLAYER -> playerBulletSpeed
            BulletType.TURRET -> turretBulletSpeed
        }

        val bullet = entityService.create(pos.x, pos.y)
        bullet.add(
            Bullet(shooter.id, pos),
            Sprite("bullet", 8f, 8f),
            Velocity(direction.x, direction.y, speed),
            Collision(
                Bounds(0.25f, 0.25f, 0f, -0.25f),
                false
            )
        )
    }
}