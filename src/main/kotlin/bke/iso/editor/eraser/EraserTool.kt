package bke.iso.editor.eraser

import bke.iso.editor.EditorCommand
import bke.iso.editor.EditorTool
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
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

        highlighted?.let { (_, box) ->
            renderer.fgShapes.addBox(box, 1f, Color.RED)
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
        val obj = highlighted?.obj
        return  if (obj is Actor) {
            DeleteActorCommand(world, obj)
        } else {
            null
        }
    }

    override fun enable() {
    }

    override fun disable() {
    }

    private data class Selection(
        val obj: GameObject,
        val box: Box
    )
}
