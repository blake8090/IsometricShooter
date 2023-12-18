package bke.iso.editor.tool

import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PointerTool(
    override val collisions: Collisions,
    private val renderer: Renderer
) : EditorTool() {

    private var highlighted: PickedActor? = null
    private var selected: PickedActor? = null

    override fun update(pointerPos: Vector3) {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        highlighted = pickActor(pointerPos)

        drawSelectionBox(highlighted, Color.WHITE)
        drawSelectionBox(selected, Color.RED)
    }

    private fun drawSelectionBox(selection: PickedActor?, color: Color) {
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

    override fun disable() {
        highlighted = null
        selected = null
    }
}
