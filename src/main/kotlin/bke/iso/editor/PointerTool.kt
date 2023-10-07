package bke.iso.editor

import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PointerTool(
    private val collisions: Collisions,
    private val renderer: Renderer
) : EditorTool {

    override fun update() {
        val pos = toWorld(renderer.getCursorPos())
        renderer.fgShapes.addPoint(pos, 1f, Color.RED)

        val collision = collisions
            .checkCollisions(pos)
            .minByOrNull { collision -> getDistance(pos, collision.box) }

        if (collision != null) {
            renderer.fgShapes.addBox(collision.box, 1f, Color.WHITE)
        }
    }

    private fun getDistance(point: Vector3, box: Box): Float {
        val bottomCenter = Vector3(
            box.pos.x,
            box.pos.y,
            0f
        )
        return point.dst(bottomCenter)
    }

    override fun performAction(): EditorCommand? {
        return null
    }

    override fun enable() {
    }

    override fun disable() {
    }
}
