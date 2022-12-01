package bke.iso.engine.physics

import bke.iso.engine.Engine
import bke.iso.engine.event.EventHandler
import bke.iso.engine.log

class MovementHandler(
    private val engine: Engine,
    private val collisionService: CollisionService
) : EventHandler<MoveEvent> {
    override fun handle(event: MoveEvent) {
        val entity = event.entity
        var dx = event.dx * engine.deltaTime
        var dy = event.dy * engine.deltaTime

        val result = collisionService.checkProjectedCollisions(entity, dx, dy)
        if (result != null) {
            val bounds = result.bounds
            for (solidCollision in result.solidCollisions) {
                val solidArea = solidCollision.area
                when (solidCollision.side) {
                    CollisionSide.TOP -> {
                        entity.y = solidArea.y - bounds.length - bounds.offsetY
                        dy = 0f
                    }

                    CollisionSide.BOTTOM -> {
                        entity.y = (solidArea.y + solidArea.height) - bounds.offsetY
                        dy = 0f
                    }

                    CollisionSide.LEFT -> {
                        entity.x = (solidArea.x + solidArea.width) - bounds.offsetX
                        dx = 0f
                    }

                    CollisionSide.RIGHT -> {
                        entity.x = solidArea.x - bounds.length - bounds.offsetX
                        dx = 0f
                    }

                    else -> {
                        log.trace("Corner collision...?")
                        dx = 0f
                        dy = 0f
                    }
                }
            }
        }

        entity.x += dx
        entity.y += dy
    }
}
