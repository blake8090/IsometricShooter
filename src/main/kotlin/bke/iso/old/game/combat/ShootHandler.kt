package bke.iso.old.game.combat

import bke.iso.old.engine.entity.Entity
import bke.iso.old.engine.event.Event
import bke.iso.old.engine.event.EventHandler
import bke.iso.old.engine.physics.Velocity
import bke.iso.old.engine.physics.Bounds
import bke.iso.old.engine.physics.Collider
import bke.iso.old.engine.render.Sprite
import bke.iso.old.engine.world.WorldService
import bke.iso.old.game.entity.Bullet
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

private const val PLAYER_BULLET_SPEED = 30f
private const val TURRET_BULLET_SPEED = 30f
private const val BULLET_Z_OFFSET = 0.5f

class ShootHandler(private val worldService: WorldService) : EventHandler<ShootEvent> {
    override val type = ShootEvent::class

    override fun handle(event: ShootEvent) {
        val shooter = event.shooter
        val target = event.target

        val pos = shooter.pos
        val direction = Vector3(target).sub(pos).nor()
        val speed = when (event.type) {
            BulletType.PLAYER -> PLAYER_BULLET_SPEED
            BulletType.TURRET -> TURRET_BULLET_SPEED
        }

        val bullet = worldService.createEntity(pos.x, pos.y, pos.z + BULLET_Z_OFFSET)
        bullet.add(
            Bullet(shooter.id, pos),
            Sprite("bullet", 8f, 8f),
            Velocity(
                direction,
                Vector3(
                    speed,
                    speed,
                    speed
                )
            ),
            Collider(
                Bounds(Vector3(0.125f, 0.125f, 0.125f)),
                false
            )
        )
    }
}
