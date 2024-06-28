package bke.iso.engine.render.occlusion

import bke.iso.engine.math.Box
import bke.iso.engine.render.Occlude
import bke.iso.engine.render.gameobject.GameObjectRenderable
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.actor.has
import com.badlogic.gdx.math.Rectangle

abstract class OcclusionStrategy {

    abstract fun firstPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?)

    abstract fun secondPass(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable?)

    abstract fun endFrame()

    protected fun occludes(renderable: GameObjectRenderable, targetRenderable: GameObjectRenderable): Boolean {
        val gameObject = checkNotNull(renderable.gameObject)

        if (gameObject == targetRenderable.gameObject || !canOcclude(gameObject)) {
            return false
        }

        val bounds = checkNotNull(renderable.bounds)
        val targetBounds = checkNotNull(targetRenderable.bounds)

        val occlusionRect = Rectangle(renderable.x, renderable.y, renderable.width, renderable.height)
        val targetOcclusionRect = getTargetOcclusionRectangle(targetRenderable)

        return inFront(bounds, targetBounds) && targetOcclusionRect.overlaps(occlusionRect)
    }

    private fun canOcclude(gameObject: GameObject): Boolean {
        return if (gameObject is Tile) {
            true
        } else {
            gameObject.has<Occlude>()
        }
    }

    private fun inFront(a: Box, b: Box): Boolean {
        if (a.max.z <= b.min.z) {
            return false
        }

        if (a.min.y - b.max.y >= 0) {
            return false
        }

        if (a.max.x - b.min.x <= 0) {
            return false
        }

        return true
    }

    private fun getTargetOcclusionRectangle(targetRenderable: GameObjectRenderable): Rectangle {
        val w = 75f
        val h = 75f
        val x = targetRenderable.x - (w / 2f) + (targetRenderable.width / 2f)
        val y = targetRenderable.y - (h / 2f) + (targetRenderable.height / 2f)
        return Rectangle(x, y, w, h)
    }
}
