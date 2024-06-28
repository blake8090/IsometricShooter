package bke.iso.engine.render.gameobject

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.toScreen
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.SpriteFillColor
import bke.iso.engine.render.SpriteTintColor
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.render.occlusion.Occlusion
import bke.iso.engine.render.withColor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool

class GameObjectRenderer(
    private val assets: Assets,
    private val world: World,
    private val events: Game.Events,
    private val debug: DebugRenderer,
    private val occlusion: Occlusion
) {

    private val pool = object : Pool<GameObjectRenderable>() {
        override fun newObject() = GameObjectRenderable()
    }

    private val renderables = Array<GameObjectRenderable>()

    fun draw(batch: PolygonSpriteBatch) {
        for (gameObject in world.getObjects()) {
            addRenderable(gameObject)
        }

        for (i in 0..<renderables.size) {
            val renderable = renderables[i]
            sortRenderables(i)
            occlusion.firstPass(renderable)
        }

        for (renderable in renderables) {
            draw(renderable, batch)
        }

        for (renderable in renderables) {
            pool.free(renderable)
        }

        renderables.clear()
        occlusion.endFrame()
    }

    private fun addRenderable(gameObject: GameObject) {
        val renderable = getRenderable(gameObject) ?: return
        renderables.add(renderable)

        occlusion.prepare(renderable)

        when (gameObject) {
            is Actor -> debug.category("actors").add(gameObject)
            is Tile -> debug.category("actors").add(gameObject)
        }
    }

    private fun sortRenderables(start: Int) {
        val a = renderables[start]
        val aBounds = checkNotNull(a.bounds) { "Expected bounds to not be null" }

        for (j in start + 1..<renderables.size) {
            val b = renderables[j]
            val bBounds = checkNotNull(b.bounds) { "Expected bounds to not be null" }

            if (inFront(aBounds, bBounds)) {
                a.behind.add(b)
            } else if (inFront(bBounds, aBounds)) {
                b.behind.add(a)
            }
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

        occlusion.secondPass(renderable)

        val color = Color(batch.color.r, batch.color.g, batch.color.b, renderable.alpha)
        val fillColor = renderable.fillColor
        val tintColor = renderable.tintColor

        // fill should always override tint
        if (fillColor != null) {
            batch.shader = assets.shaders["color"]
            batch.shader.setUniformf(
                "u_color",
                fillColor.r * 255,
                fillColor.g * 255,
                fillColor.b * 255,
                255f
            )
        } else if (tintColor != null) {
            color.r = tintColor.r
            color.g = tintColor.g
            color.b = tintColor.b
        }

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

            batch.shader = null
        }

        val obj = renderable.gameObject
        if (obj is Actor) {
            events.fire(DrawActorEvent(obj, batch))
        }
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

        if (gameObject is Actor) {
            gameObject.with<SpriteFillColor> { spriteFillColor ->
                renderable.fillColor = Color(spriteFillColor.r, spriteFillColor.g, spriteFillColor.b, 1f)
            }
            gameObject.with<SpriteTintColor> { spriteTintColor ->
                renderable.tintColor = Color(spriteTintColor.r, spriteTintColor.g, spriteTintColor.b, 1f)
            }
        }

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
