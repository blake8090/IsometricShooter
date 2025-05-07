package bke.iso.editor2.scene.tool

import bke.iso.editor2.EditorCommand
import bke.iso.editor2.scene.TilePrefabReference
import bke.iso.editor2.scene.WorldLogic
import bke.iso.editor2.scene.command.DeleteActorCommand
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color

class EraserTool(
    override val collisions: Collisions,
    private val worldLogic: WorldLogic,
    private val renderer: Renderer,
) : BaseTool() {

    private var previousType: Type? = null
    private var highlightedActor: Actor? = null

    override fun update() {
        highlightedActor = pickActor()
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        val collisionBox = highlightedActor?.getCollisionBox()
        collisionBox?.let { renderer.fgShapes.addBox(it, 1f, Color.RED) }
    }

    override fun performAction(): EditorCommand? {
        val actor = highlightedActor
            ?: return null

        previousType = getType(actor)
        return DeleteActorCommand(worldLogic, actor)
    }

    override fun performMultiAction(): EditorCommand? {
        val actor = highlightedActor
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
