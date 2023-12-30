package bke.iso.engine.render

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.toScreen
import bke.iso.engine.world.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.game.Shadow
import bke.iso.game.weapon.Bullet
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

class GameObjectRenderer(
    private val assets: Assets,
    private val world: World,
    private val events: Game.Events
) {

    var occlusionTarget: Actor? = null
    private val hiddenLayers = mutableSetOf<Float>()

    fun draw(batch: PolygonSpriteBatch) {
        val objects = world
            .getObjects()
            .asSequence()
            .mapNotNull(::map)
            .toList()


        for (i in objects.indices) {
            val a = objects[i]
            for (j in i + 1..<objects.size) {
                val b = objects[j]
                if (inFront(a, b)) {
                    a.behind.add(b)
                    b.inFront.add(a)
                } else if (inFront(b, a)) {
                    b.behind.add(a)
                    a.inFront.add(b)
                }
            }

            checkOcclusion(a)
        }

        for (renderData in objects) {
            draw(batch, renderData)
        }

        hiddenLayers.clear()
    }

    private fun checkOcclusion(renderData: ObjectRenderData) {
        val target = occlusionTarget ?: return
        val gameObject = renderData.gameObject
        // TODO: use a component to mark an object as non-occluding
        if (gameObject == target || (gameObject is Actor && (gameObject.has<Shadow>() || gameObject.has<Bullet>()))) {
            return
        }

        val targetData = map(target) ?: return
        val aRect = Rectangle(targetData.pos.x, targetData.pos.y, targetData.width, targetData.height)
        val bRect = Rectangle(renderData.pos.x, renderData.pos.y, renderData.width, renderData.height)
        if (inFront(renderData, targetData) && aRect.overlaps(bRect)) {
            renderData.alpha = 0.1f

            if (renderData.bounds.min.z >= targetData.bounds.max.z) {
                hiddenLayers.add(floor(renderData.bounds.min.z))
            }
        }
    }

    private fun draw(batch: PolygonSpriteBatch, renderData: ObjectRenderData) {
        if (renderData.visited) {
            return
        }
        renderData.visited = true
        for (a in renderData.behind) {
            draw(batch, a)
        }


        if (hiddenLayers.isNotEmpty() && floor(renderData.bounds.min.z) >= hiddenLayers.min()) {
            renderData.alpha = 0f
        }

        val color = Color(batch.color.r, batch.color.g, batch.color.b, renderData.alpha)
        batch.withColor(color) {
            batch.draw(
                /* region = */ TextureRegion(renderData.texture),
                /* x = */ renderData.pos.x,
                /* y = */ renderData.pos.y,
                /* originX = */ renderData.width / 2f,
                /* originY = */ renderData.height / 2f,
                /* width = */ renderData.width,
                /* height = */ renderData.height,
                /* scaleX = */ 1f,
                /* scaleY = */ 1f,
                /* rotation = */ renderData.rotation
            )
        }

        if (renderData.gameObject is Actor) {
            events.fire(DrawActorEvent(renderData.gameObject, batch))
        }
    }

    private fun map(gameObject: GameObject): ObjectRenderData? {
        val sprite = getSprite(gameObject)
        if (sprite == null || sprite.texture.isBlank()) {
            return null
        }

        val worldPos =
            when (gameObject) {
                is Actor -> {
                    gameObject.pos
                }

                is Tile -> {
                    gameObject.location.toVector3()
                }

                else -> {
                    error("unexpected gameobject")
                }
            }

        val texture = assets.get<Texture>(sprite.texture)

        val offset = Vector2(sprite.offsetX, sprite.offsetY)
        val pos = toScreen(worldPos).sub(offset)

        val width = texture.width * sprite.scale
        val height = texture.height * sprite.scale

        // when scaling textures, make sure texture is still centered on origin point
        if (sprite.scale != 1f) {
            val diffX = texture.width - width
            val diffY = texture.height - height
            pos.add(diffX / 2f, diffY / 2f)
        }

        val bounds = gameObject.getCollisionBox() ?: Box.fromMinMax(worldPos, worldPos)

        return ObjectRenderData(
            gameObject,
            texture,
            pos,
            width,
            height,
            sprite.alpha,
            sprite.rotation,
            bounds
        )
    }

    private fun getSprite(gameObject: GameObject): Sprite? =
        when (gameObject) {
            is Tile -> gameObject.sprite
            is Actor -> gameObject.get<Sprite>()
            else -> null
        }

    private fun inFront(a: ObjectRenderData, b: ObjectRenderData): Boolean {
        if (a.bounds.max.z <= b.bounds.min.z) {
            return false
        }

        if (a.bounds.min.y - b.bounds.max.y >= 0) {
            return false
        }

        if (a.bounds.max.x - b.bounds.min.x <= 0) {
            return false
        }

        return true
    }

    data class DrawActorEvent(
        val actor: Actor,
        val batch: PolygonSpriteBatch
    ) : Event
}

private data class ObjectRenderData(
    val gameObject: GameObject,
    val texture: Texture,
    val pos: Vector2,
    val width: Float,
    val height: Float,
    var alpha: Float,
    val rotation: Float,
    val bounds: Box,
) {
    val behind = mutableSetOf<ObjectRenderData>()
    val inFront = mutableSetOf<ObjectRenderData>()
    var visited = false
}
