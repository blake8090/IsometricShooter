package bke.iso.engine.render.occlusion

import bke.iso.engine.math.Box
import bke.iso.engine.render.Occlude
import bke.iso.engine.render.actor.ActorRenderable
import com.badlogic.gdx.math.Rectangle

abstract class OcclusionStrategy {

    abstract fun firstPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?)

    abstract fun secondPass(renderable: ActorRenderable, targetRenderable: ActorRenderable?)

    abstract fun endFrame()

    protected fun occludes(renderable: ActorRenderable, targetRenderable: ActorRenderable): Boolean {
        val actor = checkNotNull(renderable.actor)

        if (actor == targetRenderable.actor || !actor.has<Occlude>()) {
            return false
        }

        val bounds = checkNotNull(renderable.bounds)
        val targetBounds = checkNotNull(targetRenderable.bounds)

        val occlusionRect = Rectangle(renderable.x, renderable.y, renderable.width, renderable.height)
        val targetOcclusionRect = getTargetOcclusionRectangle(targetRenderable)

        return inFront(bounds, targetBounds) && targetOcclusionRect.overlaps(occlusionRect)
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

    private fun getTargetOcclusionRectangle(targetRenderable: ActorRenderable): Rectangle {
        val w = 75f
        val h = 75f
        val x = targetRenderable.x - (w / 2f) + (targetRenderable.width / 2f)
        val y = targetRenderable.y - (h / 2f) + (targetRenderable.height / 2f)
        return Rectangle(x, y, w, h)
    }
}
