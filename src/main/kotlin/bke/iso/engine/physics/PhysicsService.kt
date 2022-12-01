package bke.iso.engine.physics

import bke.iso.app.service.Service
import bke.iso.engine.entity.Entity
import bke.iso.engine.log

@Service
class PhysicsService(private val collisionService: CollisionService) {
    fun move(entity: Entity, dx: Float, dy: Float) {
        val result = collisionService.checkProjectedCollisions(entity, dx, dy)
        if (result == null || result.solidCollisions.isEmpty()) {
            entity.x += dx
            entity.y += dy
            return
        }

        log.trace("begin collision resolution")
        val bounds = result.bounds
        var newDx = dx
        var newDy = dy
        for (solidCollision in result.solidCollisions) {
            val solidArea = solidCollision.area
            when (solidCollision.side) {
                CollisionSide.TOP -> {
                    entity.y = solidArea.y - bounds.length - bounds.offsetY
                    newDy = 0f
                }

                CollisionSide.BOTTOM -> {
                    entity.y = (solidArea.y + solidArea.height) - bounds.offsetY
                    newDy = 0f
                }

                CollisionSide.LEFT -> {
                    entity.x = (solidArea.x + solidArea.width) - bounds.offsetX
                    newDx = 0f
                }

                CollisionSide.RIGHT -> {
                    entity.x = solidArea.x - bounds.length - bounds.offsetX
                    newDx = 0f
                }

                else -> {
                    log.trace("Corner collision...?")
                }
            }
            entity.x += newDx
            entity.y += newDy
            log.trace("end collision resolution")
        }
    }
}
