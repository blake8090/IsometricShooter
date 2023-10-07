package bke.iso.editor

import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.GameObject
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PointerTool(
    private val collisions: Collisions,
    private val renderer: Renderer
) : EditorTool {

    private var highlighted: Selection? = null
    private var selected: Selection? = null

    override fun update() {
        setHighlightedObject()

        highlighted?.let { (_, box) ->
            renderer.fgShapes.addBox(box, 1f, Color.WHITE)
        }

        selected?.let { (_, box) ->
            renderer.fgShapes.addBox(box, 1f, Color.RED)
        }
    }

    private fun setHighlightedObject() {
        val pos = toWorld(renderer.getCursorPos())
        renderer.fgShapes.addPoint(pos, 1f, Color.RED)

        val collision = collisions
            .checkCollisions(pos)
            .minByOrNull { collision -> getDistance(pos, collision.box) }

        highlighted =
            if (collision == null) {
                null
            } else {
                Selection(collision.obj, collision.box)
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
        val (obj, box) = highlighted ?: return null
        selected = Selection(obj, box)
        return null
    }

    override fun enable() {
    }

    override fun disable() {
        highlighted = null
        selected = null
    }

    private data class Selection(
        val obj: GameObject,
        val box: Box
    )
}
