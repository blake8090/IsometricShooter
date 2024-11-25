package bke.iso.editorv2.scene.tool

import bke.iso.editorv2.EditorCommand
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.toWorld
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

abstract class SceneTabTool {

    protected abstract val collisions: Collisions

    protected val pointerPos = Vector3()

    fun update(selectedLayer: Float, pointerPos: Vector2) {
        this.pointerPos.set(toWorld(pointerPos, selectedLayer))
        update()
    }

    protected abstract fun update()

    abstract fun draw()

    /**
     * Returns a command to be executed when the button is pressed only once.
     */
    abstract fun performAction(): EditorCommand?

    /**
     * Returns a command to be executed each frame the button is held down.
     */
    abstract fun performMultiAction(): EditorCommand?

    /**
     * Returns a command to be executed when the button is released.
     */
    abstract fun performReleaseAction(): EditorCommand?

    /**
     * Called when the tool has been selected.
     */
    open fun enable() {}

    /**
     * Called when another tool has been selected instead.
     */
    open fun disable() {}

    // TODO: use the field instead of passing the point?
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
