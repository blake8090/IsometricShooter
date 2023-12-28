package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import bke.iso.game.Shadow
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class PlayerViewSystem(
    private val world: World,
    private val renderer: Renderer
) : System {

    private val hiddenObjects = mutableSetOf<Actor>()

    override fun update(deltaTime: Float) {
        for (actor in hiddenObjects) {
            actor.with<Sprite> { it.alpha = 1f }
        }
        hiddenObjects.clear()

        val playerActor = world.actors.find<Player>() ?: return
        val collisionBox = checkNotNull(playerActor.getCollisionBox())
        val area = Box(
            pos = collisionBox.pos,
            size = Vector3(1.5f, 2f, 1f)
        )
//        val area = playerActor.getCollisionBox()
//            ?.expand(2f, 2f, 0f)
//            ?: return
        renderer.debug.addBox(area, 2f, Color.RED)

        for (gameObject in world.getObjectsInArea(area)) {
            if (gameObject !is Actor || gameObject == playerActor || gameObject.has<Shadow>()) {
                continue
            }

            val box2 = gameObject.getCollisionBox() ?: continue

            if (inFront(box2, collisionBox)) {
                gameObject.with<Sprite> {
                    it.alpha = 0.1f
                }
                hiddenObjects.add(gameObject)
            }
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
}
