package bke.iso.game

import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.Event
import bke.iso.engine.event.EventHandler
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.engine.render.Sprite
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
        bullet.vx = direction.x * speed
        bullet.vy = direction.y * speed
        bullet.add(
            Bullet(shooter.id),
            Sprite("circle", 8f, 16f),
            Collision(
                Bounds(0.25f, 0.25f, 0f, 0f),
                false
            )
        )
    }
}
