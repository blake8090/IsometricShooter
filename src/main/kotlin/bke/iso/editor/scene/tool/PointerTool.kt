package bke.iso.editor.scene.tool

import bke.iso.editor.core.command.EditorCommand
import bke.iso.editor.scene.SceneEditor
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PointerTool(
    override val collisions: Collisions,
    private val renderer: Renderer,
    private val events: Events,
    private val collisionBoxes: CollisionBoxes
) : BaseTool() {

    private var highlighted: Entity? = null

    override fun update() {
        highlighted = pickEntity()
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        highlighted?.let { entity ->
            val collisionBox = collisionBoxes[entity] ?: Box(entity.pos, Vector3(1f, 1f, 1f))
            renderer.fgShapes.addBox(collisionBox, 1f, Color.WHITE)
        }
    }

    override fun performAction(): EditorCommand? {
        highlighted?.let { entity ->
            events.fire(SceneEditor.EntitySelected(entity))
        }
        // we don't need to undo or redo selecting an entity, so no commands necessary
        return null
    }

    override fun performMultiAction(): EditorCommand? = null

    override fun performReleaseAction(): EditorCommand? = null

    override fun disable() {
        highlighted = null
    }
}
