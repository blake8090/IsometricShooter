package bke.iso.editor

import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PointerTool(
    private val collisions: Collisions,
    private val renderer: Renderer
) : EditorTool {

    private var highlighted: Selection? = null
    private var selected: Selection? = null

    override fun update() {
        val pos = toWorld(renderer.getCursorPos())
        renderer.fgShapes.addPoint(pos, 1f, Color.RED)

        highlighted = getSelection(pos)

        drawSelectionBox(highlighted, Color.WHITE)
        drawSelectionBox(selected, Color.RED)
    }

    private fun getSelection(point: Vector3): Selection? {
        val collision = collisions
            .checkCollisions(point)
            .minByOrNull { collision -> getDistance(point, collision.box) }
            ?: return null

        return if (collision.obj is Actor) {
            Selection(collision.obj, collision.box)
        } else {
            null
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

    private fun drawSelectionBox(selection: Selection?, color: Color) {
        if (selection == null) {
            return
        }
        renderer.fgShapes.addBox(selection.box, 1f, color)
    }

    override fun performAction(): EditorCommand? {
        if (highlighted != null) {
            selected = highlighted
        }
        return null
    }

    override fun performMultiAction(): EditorCommand? = null

    override fun enable() {
    }

    override fun disable() {
        highlighted = null
        selected = null
    }

    private data class Selection(
        val obj: Actor,
        val box: Box
    )
}
