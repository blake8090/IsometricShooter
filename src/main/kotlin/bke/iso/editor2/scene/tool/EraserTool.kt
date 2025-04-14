package bke.iso.editor2.scene.tool

import bke.iso.editor.scene.TilePrefabReference
import bke.iso.editor2.EditorCommand
import bke.iso.editor2.scene.WorldLogic
import bke.iso.editor2.scene.command.DeleteActorCommand
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color

class EraserTool(
    override val collisions: Collisions,
    private val worldLogic: WorldLogic,
    private val renderer: Renderer,
) : BaseTool() {

    private var previousType: Type? = null
    private var highlighted: PickedActor? = null

    override fun update() {
        val picked = pickActor()

        highlighted = picked
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        val picked = pickActor()
        if (picked != null) {
            renderer.fgShapes.addBox(picked.box, 1f, Color.RED)
        }
    }

    override fun performAction(): EditorCommand? {
        val actor = highlighted
            ?.actor
            ?: return null

        previousType = getType(actor)
        return DeleteActorCommand(worldLogic, actor)
    }

    override fun performMultiAction(): EditorCommand? {
        val actor = highlighted
            ?.actor
            ?: return null

        val type = getType(actor)
        // avoids accidentally deleting tiles underneath an actor
        return if (type == Type.TILE && previousType == Type.TILE) {
            previousType = type
            DeleteActorCommand(worldLogic, actor)
        } else {
            null
        }
    }

    override fun performReleaseAction(): EditorCommand? = null

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
