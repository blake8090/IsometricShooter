package bke.iso.game.event

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.Event
import bke.iso.engine.event.EventHandler
import bke.iso.engine.physics.Velocity
import bke.iso.engine.physics.collision.Bounds
import bke.iso.engine.physics.collision.Collider
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

        val bullet = worldService.createEntity(pos.x, pos.y, pos.z + 0.5f)
        bullet.add(
            Bullet(shooter.id, pos),
            Sprite("bullet", 8f, 8f),
            Velocity(
                direction,
                Vector3(
                    speed,
                    speed,
                    0f
                )
            ),
            Collider(
                Bounds(Vector3(0.125f, 0.125f, 0.125f)),
                false
            )
        )
    }
}
