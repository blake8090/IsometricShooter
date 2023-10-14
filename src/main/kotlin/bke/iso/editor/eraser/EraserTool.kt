package bke.iso.editor.eraser

import bke.iso.editor.EditorCommand
import bke.iso.editor.EditorTool
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class EraserTool(
    private val world: World,
    private val renderer: Renderer,
    private val collisions: Collisions
) : EditorTool {

    private var highlighted: Selection? = null

    override fun update() {
        val cursorPos = toWorld(renderer.getCursorPos())
        renderer.fgShapes.addPoint(cursorPos, 1f, Color.RED)

        val selection = getSelection(cursorPos)
        if (selection != null) {
            renderer.fgShapes.addBox(selection.box, 1f, Color.RED)
        }

        highlighted = selection
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

    override fun performAction(): EditorCommand? {
        val actor = highlighted?.actor
        return if (actor != null) {
            DeleteActorCommand(world, actor)
        } else {
            null
        }
    }

    override fun enable() {
    }

    override fun disable() {
    }

    private data class Selection(
        val actor: Actor,
        val box: Box
    )
}
