package bke.iso.editor.tool.eraser

import bke.iso.editor.tool.EditorCommand
import bke.iso.editor.tool.EditorTool
import bke.iso.editor.ReferenceActors
import bke.iso.editor.TilePrefabReference
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class EraserTool(
    override val collisions: Collisions,
    private val referenceActors: ReferenceActors,
    private val renderer: Renderer,
) : EditorTool() {

    private var previousType: Type? = null
    private var highlighted: PickedActor? = null

    override fun update(pointerPos: Vector3) {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        val picked = pickActor(pointerPos)
        if (picked != null) {
            renderer.fgShapes.addBox(picked.box, 1f, Color.RED)
        }

        highlighted = picked
    }

    override fun performAction(): EditorCommand? {
        val actor = highlighted
            ?.actor
            ?: return null

        previousType = getType(actor)
        return DeleteActorCommand(referenceActors, actor)
    }

    override fun performMultiAction(): EditorCommand? {
        val actor = highlighted
            ?.actor
            ?: return null

        val type = getType(actor)
        // avoids accidentally deleting tiles underneath an actor
        return if (type == Type.TILE && previousType == Type.TILE) {
            previousType = type
            DeleteActorCommand(referenceActors, actor)
        } else {
            null
        }
    }

    private fun getType(actor: Actor) =
        if (actor.has<TilePrefabReference>()) {
            Type.TILE
        } else {
            Type.ACTOR
        }

    private enum class Type {
        ACTOR,
        TILE
    }
}
