package bke.iso.editor2.scene.tool

import bke.iso.editor2.EditorCommand
import bke.iso.editor2.scene.SceneMode
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PointerTool(
    override val collisions: Collisions,
    private val renderer: Renderer,
    private val events: Events
) : BaseTool() {

    private var highlighted: Actor? = null

    override fun update() {
        highlighted = pickActor()
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        highlighted?.let { actor ->
            val collisionBox = actor.getCollisionBox() ?: Box(actor.pos, Vector3(1f, 1f, 1f))
            renderer.fgShapes.addBox(collisionBox, 1f, Color.WHITE)
        }
    }

    override fun performAction(): EditorCommand? {
        highlighted?.let { actor ->
            events.fire(SceneMode.ActorSelected(actor))
        }
        // we don't need to undo or redo selecting an actor, so no commands necessary
        return null
    }

    override fun performMultiAction(): EditorCommand? = null

    override fun performReleaseAction(): EditorCommand? = null

    override fun disable() {
        highlighted = null
    }
}
