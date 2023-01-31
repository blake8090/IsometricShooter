package bke.iso.game.event

import bke.iso.service.Transient
import bke.iso.engine.entity.Entity
import bke.iso.engine.event.Event
import bke.iso.engine.event.EventHandler
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.engine.physics.Velocity
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.WorldService
import bke.iso.game.Bullet
import com.badlogic.gdx.math.Vector3

enum class BulletType {
    PLAYER,
    TURRET
}

data class ShootEvent(
    val shooter: Entity,
    val target: Vector3,
    val type: BulletType
) : Event()

@Transient
class ShootHandler(private val worldService: WorldService) : EventHandler<ShootEvent> {
    override val type = ShootEvent::class

    private val playerBulletSpeed = 30f
    private val turretBulletSpeed = 20f

    override fun handle(event: ShootEvent) {
        val shooter = event.shooter
        val target = event.target

        val pos = Vector3(shooter.x, shooter.y, shooter.z)
        val direction = Vector3(target).sub(pos).nor()
        val speed = when (event.type) {
            BulletType.PLAYER -> playerBulletSpeed
            BulletType.TURRET -> turretBulletSpeed
        }

        val bullet = worldService.createEntity(pos.x, pos.y, pos.z)
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
