package bke.iso.editor

import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.TILE_SIZE_Z
import bke.iso.engine.math.toWorld
import bke.iso.engine.world.Actor
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

interface EditorCommand {
    fun execute()
    fun undo()
}

abstract class EditorTool {

    protected abstract val collisions: Collisions

    fun update(selectedLayer: Float, pointerPos: Vector2) {
        // fixes an issue where actors placed on different layers don't match the pointer's position
        pointerPos.y -= (TILE_SIZE_Z * selectedLayer)

        val worldPos = toWorld(pointerPos)
        worldPos.z = selectedLayer

        update(worldPos)
    }

    protected abstract fun update(pointerPos: Vector3)

    /**
     * Returns a command to be executed when the button is pressed only once.
     */
    abstract fun performAction(): EditorCommand?

    /**
     * Returns a command to be executed each frame the button is held down.
     */
    abstract fun performMultiAction(): EditorCommand?

    /**
     * Called when the tool has been selected.
     */
    open fun enable() {}

    /**
     * Called when another tool has been selected instead.
     */
    open fun disable() {}

    protected fun pickActor(point: Vector3): PickedActor? {
        val collision = collisions
            .checkCollisions(point)
            .filter { collision -> collision.box.min.z == point.z }
            .minByOrNull { collision -> getDistance(point, collision.box) }
            ?: return null

        return if (collision.obj is Actor) {
            PickedActor(collision.obj, collision.box)
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

    protected data class PickedActor(
        val actor: Actor,
        val box: Box
    )
}
