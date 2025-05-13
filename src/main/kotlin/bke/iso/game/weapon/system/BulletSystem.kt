package bke.iso.game.weapon.system

import bke.iso.engine.state.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.nextFloat
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Description
import bke.iso.engine.world.entity.Tile
import bke.iso.game.combat.CombatModule
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

private const val MAX_BULLET_DISTANCE = 50f

class BulletSystem(
    private val world: World,
    private val combatModule: CombatModule,
    private val collisions: Collisions
) : System {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        world.entities.each<Bullet> { bulletActor, bullet ->
            update(bulletActor, bullet)
        }
    }

    private fun update(bulletEntity: Entity, bullet: Bullet) {
        val distance = bullet.start.dst(bulletEntity.pos)
        if (distance > MAX_BULLET_DISTANCE) {
            world.delete(bulletEntity)
            return
        }

        val firstCollision = collisions
            .getCollisions(bulletEntity)
            .sortedBy(Collision::distance)
            .firstOrNull { collision -> canCollide(bullet, collision.entity) }

        if (firstCollision != null) {
            handleCollision(bulletEntity, bullet, firstCollision)
        }
    }

    private fun canCollide(bullet: Bullet, entity: Entity): Boolean =
        if (entity.has<Tile>()) {
            true
        } else {
            entity.id != bullet.shooterId
                    && !entity.has<Bullet>()
                    && !entity.has<Explosion>()
        }

    private fun handleCollision(bulletEntity: Entity, bullet: Bullet, collision: Collision) {
        val target = collision.entity
        val damage = calculateDamage(bulletEntity, bullet)
        combatModule.applyDamage(target, damage)

        createExplosion(bulletEntity.pos, collision)
        world.delete(bulletEntity)
    }

    private fun calculateDamage(bulletEntity: Entity, bullet: Bullet): Float {
        val distance = bulletEntity.pos.dst(bullet.start)
        if (distance <= bullet.range) {
            return bullet.damage
        }

        val ratio = bullet.range / distance
        val damage = bullet.damage * ratio
        log.debug { "Bullet range: ${bullet.range} dist: $distance ratio: $ratio damage: $damage" }

        return damage
    }

    private fun createExplosion(pos: Vector3, collision: Collision) {
        val explosion = world.entities.create(
            pos,
            Explosion(0.25f),
            Collider(
                size = Vector3(0.25f, 0.25f, 0.25f),
                offset = Vector3(-0.125f, -0.125f, -0.125f)
            ),
            Sprite(
                "bullet.png",
                scale = nextFloat(0.75f, 1f),
                offsetX = 8f,
                offsetY = 8f
            ),
            PhysicsBody(mode = PhysicsMode.GHOST),
            Description("explosion")
        )
        clampPosToCollisionSide(explosion, collision)
    }

    // TODO: make this a util
    private fun clampPosToCollisionSide(entity: Entity, collision: Collision) {
        val box = checkNotNull(entity.getCollisionBox()) {
            "Expected collision box for $entity"
        }
        val otherBox = checkNotNull(collision.entity.getCollisionBox()) {
            "Expected collision box for ${collision.entity}"
        }

        var x = entity.x
        var y = entity.y
        var z = entity.z
        when (collision.side) {
            CollisionSide.LEFT -> {
                x = otherBox.min.x - (box.size.x / 2f)
            }

            CollisionSide.RIGHT -> {
                x = otherBox.max.x + (box.size.x / 2f)
            }

            CollisionSide.FRONT -> {
                y = otherBox.min.y - (box.size.y / 2f)
            }

            CollisionSide.BACK -> {
                y = otherBox.max.y + (box.size.y / 2f)
            }

            CollisionSide.TOP -> {
                z = otherBox.max.z // actor origins are at the bottom of the collision box, not the center
            }

            CollisionSide.BOTTOM -> {
                z = otherBox.min.z - box.size.z // actor origins are at the bottom of the collision box, not the center
            }

            CollisionSide.CORNER -> {
                // log.warn { "Could not resolve corner collision" }
            }
        }
        entity.moveTo(x, y, z)
    }
}
