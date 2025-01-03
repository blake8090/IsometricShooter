package bke.iso.editor.scene.tool

import bke.iso.editor.EditorCommand
import bke.iso.engine.core.Events
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color

class PointerTool(
    override val collisions: Collisions,
    private val renderer: Renderer,
    private val events: Events
) : SceneTabTool() {

    private var highlighted: PickedActor? = null
    private var selected: PickedActor? = null

    override fun update() {
        highlighted = pickActor(pointerPos)
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)
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
        val currentlyHighlighted = highlighted

        if (currentlyHighlighted != null) {
            selected = currentlyHighlighted
            events.fire(PointerSelectActorEvent(currentlyHighlighted.actor))
        } else {
            events.fire(PointerDeselectActorEvent())
        }

        return null
    }

    override fun performMultiAction(): EditorCommand? = null

    override fun performReleaseAction(): EditorCommand? = null

    override fun disable() {
        highlighted = null
        selected = null
        events.fire(PointerDeselectActorEvent())
    }

    fun getSelectedActor(): Actor? =
        selected?.actor
}
