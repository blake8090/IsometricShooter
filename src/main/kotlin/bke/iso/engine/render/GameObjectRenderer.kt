package bke.iso.engine.render

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.math.toScreen
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

class GameObjectRenderer(
    private val renderer: Renderer,
    private val assets: Assets,
    private val world: World,
    private val events: Game.Events
) {

    var occlusionTarget: Actor? = null

    // TODO: just store the minimum hidden layer
    private val hiddenBuildingLayers = mutableMapOf<String, MutableSet<Float>>()

    fun draw(batch: PolygonSpriteBatch) {
        val objects = world
            .getObjects()
            .mapNotNull(::getRenderData)

        for (i in objects.indices) {
            val a = objects[i]

            for (j in i + 1..<objects.size) {
                val b = objects[j]

                if (inFront(a, b)) {
                    a.behind.add(b)
                } else if (inFront(b, a)) {
                    b.behind.add(a)
                }
            }

            checkOcclusion(a)

            when (val obj = a.gameObject) {
                is Actor -> renderer.debug.add(obj)
                is Tile -> renderer.debug.add(obj)
            }
        }

        for (renderData in objects) {
            draw(batch, renderData)
        }

        hiddenBuildingLayers.clear()
    }

    private fun checkOcclusion(renderData: RenderData) {
        val target = occlusionTarget ?: return
        val gameObject = renderData.gameObject
        if (gameObject == target || (gameObject is Actor && !gameObject.has<Occlude>())) {
            return
        }

        val targetData = getRenderData(target) ?: return
        val aRect = getOcclusionRectangle(targetData)
        renderer.debug.addRectangle(aRect, 1f, Color.RED)

        val bRect = Rectangle(renderData.pos.x, renderData.pos.y, renderData.width, renderData.height)
        if (inFront(renderData, targetData) && aRect.overlaps(bRect)) {
            renderData.alpha = 0.1f

            if (renderData.bounds.min.z >= targetData.bounds.max.z) {
                val layer = floor(renderData.bounds.min.z)

                val building = world.buildings.getBuilding(renderData.gameObject)
                if (!building.isNullOrBlank()) {
                    hiddenBuildingLayers
                        .getOrPut(building) { mutableSetOf() }
                        .add(layer)
                }
            }
        }
    }

    // TODO: make this more configurable - refactor into an occlusion strategy class or something
    private fun getOcclusionRectangle(targetData: RenderData): Rectangle {
        val w = 75f
        val h = 75f
        val x = targetData.pos.x - (w / 2f) + (targetData.width / 2f)
        val y = targetData.pos.y - (h / 2f) + (targetData.height / 2f)
        return Rectangle(x, y, w, h)
    }

    private fun draw(batch: PolygonSpriteBatch, renderData: RenderData) {
        if (renderData.visited) {
            return
        }
        renderData.visited = true

        for (data in renderData.behind) {
            draw(batch, data)
        }

        val building = world.buildings.getBuilding(renderData.gameObject)
        val hiddenLayers = hiddenBuildingLayers[building] ?: emptySet()
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

    private fun getRenderData(gameObject: GameObject): RenderData? {
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

        return RenderData(
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

    private fun getPos(gameObject: GameObject) =
        when (gameObject) {
            is Actor -> gameObject.pos
            is Tile -> gameObject.location.toVector3()
            else -> error("unexpected GameObject ${gameObject::class.simpleName}")
        }

    private fun inFront(a: RenderData, b: RenderData): Boolean {
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

// TODO: pool instances of this
private data class RenderData(
    val gameObject: GameObject,
    val texture: Texture,
    val pos: Vector2,
    val width: Float,
    val height: Float,
    var alpha: Float,
    val rotation: Float,
    val bounds: Box
) {
    val behind = mutableListOf<RenderData>()
    var visited = false
}
