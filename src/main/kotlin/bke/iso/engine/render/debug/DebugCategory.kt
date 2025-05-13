package bke.iso.engine.render.debug

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.DebugSettings
import bke.iso.engine.render.shape.ShapeArray
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3

class DebugCategory {

    var enabled = false
    val shapes = ShapeArray()

    fun addLine(start: Vector3, end: Vector3, width: Float, color: Color) {
        if (enabled) {
            shapes.addLine(start, end, width, color)
        }
    }

    fun addPoint(pos: Vector3, size: Float, color: Color) {
        if (enabled) {
            shapes.addPoint(pos, size, color)
        }
    }

    fun addBox(box: Box, width: Float, color: Color) {
        if (enabled) {
            shapes.addBox(box, width, color)
        }
    }

    fun addSphere(pos: Vector3, radius: Float, color: Color) {
        if (enabled) {
            shapes.addSphere(pos, radius, color)
        }
    }

    fun add(entity: Entity) {
        if (!enabled) {
            return
        }
        val settings = entity.get<DebugSettings>() ?: return
        if (settings.collisionBox) {
            val color = if (settings.collisionBoxSelected) Color.PURPLE else settings.collisionBoxColor
            entity.getCollisionBox()?.let { box ->
                addBox(box, 1f, color)
            }
            settings.collisionBoxSelected = false
        }

        if (settings.position) {
            addPoint(entity.pos, 2f, settings.positionColor)
        }

        if (settings.zAxis && entity.z > 0f) {
            val start = Vector3(entity.x, entity.y, 0f)
            val end = entity.pos
            addPoint(start, 2f, settings.zAxisColor)
            addLine(start, end, 1f, settings.zAxisColor)
        }
    }

//    fun add(tile: Tile) {
//        if (!enabled) {
//            return
//        }
//        val color = if (tile.selected) Color.PURPLE else Color.WHITE
//        addBox(tile.getCollisionBox(), 1f, color)
//        tile.selected = false
//    }

    fun addRectangle(rectangle: Rectangle, lineWidth: Float, color: Color) {
        if (enabled) {
            shapes.addRectangle(rectangle, lineWidth, color)
        }
    }

    fun clear() {
        shapes.clear()
    }
}
