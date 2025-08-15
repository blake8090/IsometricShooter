package bke.iso.engine.render.entity

import bke.iso.engine.lighting.Lighting
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
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
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.Pool
import kotlin.math.floor

/**
 * This renderer has massive performance gains due to grouping renderables by rows (y-axis)
 * instead of grouping by layer (z-axis).
 *
 * Consider that [OptimizedEntityRenderer.sortRenderables] is of quadratic time complexity.
 * When renderables are grouped by layer, there are fewer but larger lists, as many objects share the same layer.
 * When grouped by rows, there are numerous but much smaller lists, since most objects do not share the same row.
 *
 * Since quadratic algorithms perform best on very small lists, grouping by rows will net significant performance gains.
 */
class OptimizedEntityRenderer(
    private val assets: Assets,
    private val events: Events,
    private val debug: DebugRenderer,
    private val occlusion: Occlusion,
    private val camera: OrthographicCamera,
    private val lighting: Lighting
) {

    private val pool = object : Pool<EntityRenderable>() {
        override fun newObject() = EntityRenderable()
    }

    private val tempEvent = DrawEntityEvent(null)
    private val tempPos = Vector2()

    private val renderablesByRow = OrderedMap<Float, Array<EntityRenderable>>()

    fun draw(batch: PolygonSpriteBatch, world: World) {
        for (entity in world.entities) {
            addRenderable(entity)
        }

        // we sort by descending here as isometric objects must be drawn from back-to-front, not front-to-back.
        val keys = renderablesByRow.orderedKeys()
        keys.sort()
        keys.reverse()

        for (i in 0..<keys.size) {
            val row = keys[i]
            val renderables = renderablesByRow.get(row) ?: continue

            for (i in 0..<renderables.size) {
                val renderable = renderables[i]
                sortRenderables(renderables, i)
                occlusion.firstPass(renderable)
            }

            for (renderable in renderables) {
                draw(renderable, batch)
            }
        }

        for (renderables in renderablesByRow.values()) {
            pool.freeAll(renderables)
        }

        renderablesByRow.clear()
        occlusion.endFrame()
    }

    private fun addRenderable(entity: Entity) {
        val renderable = getRenderable(entity)
        if (renderable == null || !inFrustum(renderable)) {
            return
        }

        val bounds = checkNotNull(renderable.bounds) { "Expected bounds to not be null" }
        val row = floor(bounds.min.y)

        if (!renderablesByRow.containsKey(row)) {
            renderablesByRow.put(row, Array())
        }
        renderablesByRow
            .get(row)
            .add(renderable)

        occlusion.prepare(renderable)

        debug.category("render").addBox(bounds, 1f, Color.GREEN)
    }

    private fun inFrustum(renderable: EntityRenderable): Boolean =
        camera.frustum.boundsInFrustum(
            /* x = */ renderable.x,
            /* y = */ renderable.y,
            /* z = */ 0f,
            /* halfWidth = */ renderable.width,
            /* halfHeight = */ renderable.height,
            /* halfDepth = */ 0f
        )

    private fun sortRenderables(renderables: Array<EntityRenderable>, start: Int) {
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

    private fun draw(renderable: EntityRenderable, batch: PolygonSpriteBatch) {
        if (renderable.visited) {
            return
        }
        renderable.visited = true

        for (data in renderable.behind) {
            draw(data, batch)
        }

        occlusion.secondPass(renderable)

        if (renderable.alpha == 0f) {
            return
        }

        val color = Color(batch.color.r, batch.color.g, batch.color.b, renderable.alpha)
        val fillColor = renderable.fillColor
        val tintColor = renderable.tintColor

        var shaderSet = false

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
            shaderSet = true
        } else if (tintColor != null) {
            color.r = tintColor.r
            color.g = tintColor.g
            color.b = tintColor.b
        }

        val (r, g, b) = lighting.getColor(renderable.entity!!)
        color.r = r
        color.g = g
        color.b = b

        batch.withColor(color) {
            batch.draw(
                /* region = */ renderable.texture,
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

            // changing the shader even to null is expensive, so only do it for entities that actually switched shaders
            if (shaderSet) {
                batch.shader = null
            }
        }

        renderable.entity?.let { entity ->
            tempEvent.entity = entity
            tempEvent.batch = batch
            events.fire(tempEvent)
        }
    }

    private fun getRenderable(entity: Entity): EntityRenderable? {
        val sprite = entity.get<Sprite>()
        if (sprite == null || sprite.texture.isBlank()) {
            return null
        }

        val worldPos = entity.pos
        toScreen(entity.x, entity.y, entity.z, tempPos)
        tempPos.sub(sprite.offsetX, sprite.offsetY)

        val textureRegion = assets.textures.findRegion(sprite.texture)
        val width = textureRegion.regionWidth * sprite.scale
        val height = textureRegion.regionHeight * sprite.scale

        // when scaling textures, make sure texture is still centered on origin point
        if (sprite.scale != 1f) {
            val diffX = textureRegion.regionWidth - width
            val diffY = textureRegion.regionHeight - height
            tempPos.add(diffX / 2f, diffY / 2f)
        }

        val bounds = entity.getCollisionBox() ?: Box.fromMinMax(worldPos, worldPos)

        val renderable = pool.obtain()
        renderable.entity = entity
        renderable.texture = textureRegion
        renderable.bounds = bounds
        renderable.x = tempPos.x
        renderable.y = tempPos.y
        renderable.offsetX = sprite.offsetX
        renderable.offsetY = sprite.offsetY
        renderable.width = width
        renderable.height = height
        renderable.alpha = sprite.alpha
        renderable.rotation = sprite.rotation

        entity.with<SpriteFillColor> { spriteFillColor ->
            renderable.fillColor = Color(spriteFillColor.r, spriteFillColor.g, spriteFillColor.b, 1f)
        }
        entity.with<SpriteTintColor> { spriteTintColor ->
            renderable.tintColor = Color(spriteTintColor.r, spriteTintColor.g, spriteTintColor.b, 1f)
        }

        return renderable
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

    data class DrawEntityEvent(
        var entity: Entity? = null,
        var batch: PolygonSpriteBatch? = null
    ) : Event
}
