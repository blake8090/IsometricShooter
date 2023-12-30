package bke.iso.game.player

import bke.iso.engine.System
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.TILE_SIZE_Y
import bke.iso.engine.math.TILE_SIZE_Z
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.VIRTUAL_HEIGHT
import bke.iso.engine.render.VIRTUAL_WIDTH
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.game.Shadow
import bke.iso.game.weapon.Bullet
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import kotlin.math.ceil
import kotlin.math.floor

class PlayerViewSystem(
    private val world: World,
    private val renderer: Renderer
) : System {

    private val hiddenObjects = mutableSetOf<GameObject>()

    override fun update(deltaTime: Float) {
        resetHiddenObjects()

        val playerActor = world.actors.find<Player>() ?: return
        val collisionBox = checkNotNull(playerActor.getCollisionBox())

        val w = ceil(VIRTUAL_WIDTH / TILE_SIZE_Y)
        val h = ceil(VIRTUAL_HEIGHT / TILE_SIZE_Z)
        val area = Box(
            pos = collisionBox.pos,
            size = Vector3(w, w, h)
        )
        renderer.debug.addBox(area, 2f, Color.RED)

        val rect = checkNotNull(getTextureRectangle(playerActor))

        val objects = world.getObjectsInArea(area)
        val hiddenLayers = mutableSetOf<Float>()

        for (gameObject in objects) {
            if (playerActor == gameObject || (gameObject is Actor && (gameObject.has<Shadow>() || gameObject.has<Bullet>()))) {
                continue
            }

            val box = gameObject.getCollisionBox() ?: continue
            val rect2 = getTextureRectangle(gameObject) ?: continue
            if (rect2.overlaps(rect) && inFront(box, collisionBox)) {
                hideObject(gameObject, 0.1f)
                if (box.min.z >= collisionBox.max.z) {
                    hiddenLayers.add(floor(box.min.z))
                }
            }
        }

        for (gameObject in objects) {
            val box = gameObject.getCollisionBox() ?: continue
            if (hiddenLayers.isNotEmpty() && floor(box.min.z) >= hiddenLayers.min()) {
                hideObject(gameObject, 0f)
            }
        }
    }

    private fun getTextureRectangle(gameObject: GameObject): Rectangle? {
        val renderData = renderer.getRenderData(gameObject)
        if (renderData != null) {
            return Rectangle(
                renderData.pos.x,
                renderData.pos.y,
                renderData.width,
                renderData.height
            )
        }
        return null
    }

    private fun resetHiddenObjects() {
        for (gameObject in hiddenObjects) {
            if (gameObject is Actor) {
                gameObject.with<Sprite> { sprite -> sprite.alpha = 1f }
            } else if (gameObject is Tile) {
                gameObject.sprite.alpha = 1f
            }
        }
        hiddenObjects.clear()
    }

    private fun hideObject(gameObject: GameObject, alpha: Float = 0.1f) {
        if (gameObject is Actor) {
            gameObject.with<Sprite> { sprite -> sprite.alpha = alpha }
        } else if (gameObject is Tile) {
            gameObject.sprite.alpha = alpha
        }
        hiddenObjects.add(gameObject)
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
