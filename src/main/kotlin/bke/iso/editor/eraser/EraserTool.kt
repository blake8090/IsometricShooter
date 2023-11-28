package bke.iso.editor.eraser

import bke.iso.editor.EditorCommand
import bke.iso.editor.EditorTool
import bke.iso.editor.ReferenceActors
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class EraserTool(
    override val collisions: Collisions,
    private val referenceActors: ReferenceActors,
    private val renderer: Renderer,
) : EditorTool() {

    private var highlighted: PickedActor? = null

    override fun update(pointerPos: Vector3) {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        val picked = pickActor(pointerPos)
        if (picked != null) {
            renderer.fgShapes.addBox(picked.box, 1f, Color.RED)
        }

        highlighted = picked
    }

    override fun performAction(): EditorCommand? =
        deleteActor()

    // TODO: if the previous actor was a tile and the next actor is an actor, don't perform the action.
    //  avoids accidentally deleting other actors underneath a deleted actor or tile
    override fun performMultiAction(): EditorCommand? =
        deleteActor()

    private fun deleteActor(): EditorCommand? {
        val actor = highlighted?.actor
        return if (actor != null) {
            DeleteActorCommand(referenceActors, actor)
        } else {
            null
        }
    }
}
