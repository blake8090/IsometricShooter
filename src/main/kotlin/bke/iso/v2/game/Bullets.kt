package bke.iso.v2.game

import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collider
import bke.iso.engine.physics.Velocity
import bke.iso.engine.render.Sprite
import bke.iso.game.entity.Bullet
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.World
import com.badlogic.gdx.math.Vector3

private const val PLAYER_BULLET_SPEED = 30f
private const val TURRET_BULLET_SPEED = 30f
private const val BULLET_Z_OFFSET = 0.5f

enum class BulletType {
    PLAYER,
    TURRET
}

class Bullets(private val world: World) {

    fun shoot(shooter: Actor, target: Vector3, bulletType: BulletType) {
        val pos = shooter.pos
        val direction = Vector3(target).sub(pos).nor()
        val speed = when (bulletType) {
            BulletType.PLAYER -> PLAYER_BULLET_SPEED
            BulletType.TURRET -> TURRET_BULLET_SPEED
        }
        world.newActor(
            pos.x,
            pos.y,
            pos.z + BULLET_Z_OFFSET,
            Bullet(shooter.id, pos),
            Sprite("bullet", 8f, 8f),
            Velocity(direction, Vector3(speed, speed, speed)),
            Collider(
                Bounds(Vector3(0.125f, 0.125f, 0.125f)),
                false
            )
        )
    }
}
