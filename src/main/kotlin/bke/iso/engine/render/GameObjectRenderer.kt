package bke.iso.engine.render

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import kotlin.math.floor

class GameObjectRenderer(
    private val assets: Assets,
    private val world: World,
    private val events: Game.Events,
    private val debug: DebugRenderer
) {

    private val pool = object : Pool<GameObjectRenderable>() {
        override fun newObject() = GameObjectRenderable()
    }

    private val renderables = Array<GameObjectRenderable>()

    var occlusionTarget: Actor? = null
    private var occlusionTargetRenderable: GameObjectRenderable? = null

    // TODO: just store the minimum hidden layer
    private val hiddenBuildingLayers = mutableMapOf<String, MutableList<Float>>()

    fun draw(batch: PolygonSpriteBatch) {
        for (gameObject in world.getObjects()) {
            addRenderable(gameObject)
        }

        sortRenderables()

        for (renderable in renderables) {
            draw(renderable, batch)
        }

        for (renderable in renderables) {
            pool.free(renderable)
        }

        renderables.clear()
        hiddenBuildingLayers.clear()
    }

    private fun addRenderable(gameObject: GameObject) {
        val renderable = getRenderable(gameObject) ?: return
        renderables.add(renderable)

        if (gameObject == occlusionTarget) {
            occlusionTargetRenderable = renderable
        }

        when (gameObject) {
            is Actor -> debug.category("actors").add(gameObject)
            is Tile -> debug.category("actors").add(gameObject)
        }
    }

    private fun sortRenderables() {
        for (i in 0..<renderables.size) {
            val a = renderables[i]
            val aBounds = checkNotNull(a.bounds) { "Expected bounds to not be null" }

            for (j in i + 1..<renderables.size) {
                val b = renderables[j]
                val bBounds = checkNotNull(b.bounds) { "Expected bounds to not be null" }

                if (inFront(aBounds, bBounds)) {
                    a.behind.add(b)
                } else if (inFront(bBounds, aBounds)) {
                    b.behind.add(a)
                }
            }

            checkOcclusion(a)
        }
    }

    private fun draw(renderable: GameObjectRenderable, batch: PolygonSpriteBatch) {
        if (renderable.visited) {
            return
        }
        renderable.visited = true

        for (data in renderable.behind) {
            draw(data, batch)
        }

        val building = world.buildings.getBuilding(renderable.gameObject!!)
        val hiddenLayers = hiddenBuildingLayers[building] ?: emptyList()
        if (hiddenLayers.isNotEmpty() && floor(renderable.bounds!!.min.z) >= hiddenLayers.min()) {
            renderable.alpha = 0f
        }

        val color = Color(batch.color.r, batch.color.g, batch.color.b, renderable.alpha)
        batch.withColor(color) {
            batch.draw(
                /* region = */ TextureRegion(renderable.texture),
                /* x = */ renderable.x,
                /* y = */ renderable.y,
                /* originX = */ renderable.width / 2f,
                /* originY = */ renderable.height / 2f,
                /* width = */ renderable.width,
                /* height = */ renderable.height,
                /* scaleX = */ 1f,
                /* scaleY = */ 1f,
                /* rotation = */ renderable.rotation
            )
        }

        val obj = renderable.gameObject
        if (obj is Actor) {
            events.fire(DrawActorEvent(obj, batch))
        }
    }

    private fun checkOcclusion(renderable: GameObjectRenderable) {
        if (occlusionTarget == null) {
            return
        }

        val gameObject = renderable.gameObject
        if (gameObject == occlusionTarget || (gameObject is Actor && !gameObject.has<Occlude>())) {
            return
        }

        val targetRenderable = checkNotNull(occlusionTargetRenderable) {
            "Expected renderable for occlusion target $occlusionTarget"
        }

        val occlusionRect = getOcclusionRectangle(targetRenderable)
//        renderer.debug.category("occlusion").addRectangle(aRect, 1f, Color.RED)
        val rect = Rectangle(renderable.x, renderable.y, renderable.width, renderable.height)

        val bounds = checkNotNull(renderable.bounds) {
            "Expected bounds for renderable ${renderable.gameObject}"
        }
        val targetBounds = checkNotNull(targetRenderable.bounds) {
            "Expected bounds for renderable ${targetRenderable.gameObject}"
        }

        if (inFront(bounds, targetBounds) && occlusionRect.overlaps(rect)) {
            renderable.alpha = 0.1f

            if (bounds.min.z >= targetBounds.max.z) {
                val layer = floor(bounds.min.z)

                val building = world.buildings.getBuilding(renderable.gameObject!!)
                if (!building.isNullOrBlank()) {
                    hiddenBuildingLayers
                        .getOrPut(building) { mutableListOf() }
                        .add(layer)
                }
            }
        }
    }

    private fun getOcclusionRectangle(renderable: GameObjectRenderable): Rectangle {
        val w = 75f
        val h = 75f
        val x = renderable.x - (w / 2f) + (renderable.width / 2f)
        val y = renderable.y - (h / 2f) + (renderable.height / 2f)
        return Rectangle(x, y, w, h)
    }

    private fun getRenderable(gameObject: GameObject): GameObjectRenderable? {
        val sprite = getSprite(gameObject)
        if (sprite == null || sprite.texture.isBlank()) {
            return null
        }

        val worldPos = getPos(gameObject)
        val offset = Vector2(sprite.offsetX, sprite.offsetY)
        val pos = toScreen(worldPos).sub(offset)

        val texture = assets.get<Texture>(sprite.texture)
        val width = texture.width * sprite.scale
        val height = texture.height * sprite.scale

        // when scaling textures, make sure texture is still centered on origin point
        if (sprite.scale != 1f) {
            val diffX = texture.width - width
            val diffY = texture.height - height
            pos.add(diffX / 2f, diffY / 2f)
        }

        val bounds = gameObject.getCollisionBox() ?: Box.fromMinMax(worldPos, worldPos)

        val renderable = pool.obtain()
        renderable.gameObject = gameObject
        renderable.texture = texture
        renderable.bounds = bounds
        renderable.x = pos.x
        renderable.y = pos.y
        renderable.offsetX = offset.x
        renderable.offsetY = offset.y
        renderable.width = width
        renderable.height = height
        renderable.alpha = sprite.alpha
        renderable.rotation = sprite.rotation

        return renderable
    }

    private fun getSprite(gameObject: GameObject): Sprite? =
        when (gameObject) {
            is Tile -> gameObject.sprite
            is Actor -> gameObject.get<Sprite>()
            else -> null
        }

    private fun getPos(gameObject: GameObject) =
        when (gameObject) {
            is Actor -> gameObject.pos
            is Tile -> gameObject.location.toVector3()
            else -> error("unexpected GameObject ${gameObject::class.simpleName}")
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

    data class DrawActorEvent(
        val actor: Actor,
        val batch: PolygonSpriteBatch
    ) : Event
}
