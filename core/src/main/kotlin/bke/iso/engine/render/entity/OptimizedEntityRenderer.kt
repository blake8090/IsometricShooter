package bke.iso.engine.render.entity

import bke.iso.engine.lighting.Lighting
import bke.iso.engine.lighting.FullBright
import bke.iso.engine.lighting.PointLight
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.math.Box
import bke.iso.engine.math.TILE_SIZE_X
import bke.iso.engine.math.TILE_SIZE_Y
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.Pool
import kotlin.math.floor

private const val MAX_PIXEL_LIGHTS = 16
private const val SQRT_TWO = 1.41421356237f
private const val PIXEL_LIGHTING_SHADER = "pixel-lighting"

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
    private val lighting: Lighting,
    private val collisionBoxes: CollisionBoxes
) {

    private val pool = object : Pool<EntityRenderable>() {
        override fun newObject() = EntityRenderable()
    }

    private val tempEvent = DrawEntityEvent(null)
    private val tempPos = Vector2()
    private val tempLightPos = Vector3()
    private val tempLightScreenPos = Vector2()

    private val lightPositionRadii = FloatArray(MAX_PIXEL_LIGHTS * 4)
    private val lightColorIntensities = FloatArray(MAX_PIXEL_LIGHTS * 4)
    private val lightCandidates = Array<LightCandidate>(false, MAX_PIXEL_LIGHTS)
    private val lightCandidatePool = object : Pool<LightCandidate>() {
        override fun newObject() = LightCandidate()
    }

    private val renderablesByRow = OrderedMap<Float, Array<EntityRenderable>>()

    fun draw(batch: PolygonSpriteBatch, world: World) {
        // Game shaders are loaded after the main menu is already rendering, so use the default unlit shader until
        // the pixel-lighting shader becomes available.
        val lightingShader = assets.shaders[PIXEL_LIGHTING_SHADER]
        batch.shader = lightingShader
        if (lightingShader != null) {
            configureLightingShader(lightingShader)
        }

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
                draw(renderable, batch, lightingShader)
            }
        }

        for (renderables in renderablesByRow.values()) {
            pool.freeAll(renderables)
        }

        renderablesByRow.clear()
        occlusion.endFrame()
        batch.shader = null
    }

    private fun configureLightingShader(shader: ShaderProgram) {
        lightCandidatePool.freeAll(lightCandidates)
        lightCandidates.clear()

        lighting.forEachPointLight { entity: Entity, pointLight: PointLight ->
            if (pointLight.intensity <= 0f || pointLight.radius <= 0f) {
                return@forEachPointLight
            }

            tempLightPos
                .set(entity.x, entity.y, entity.z)
                .add(pointLight.offset)
            toScreen(tempLightPos.x, tempLightPos.y, tempLightPos.z, tempLightScreenPos)

            val radiusX = pointLight.radius * TILE_SIZE_X / SQRT_TWO
            val radiusY = pointLight.radius * TILE_SIZE_Y / SQRT_TWO
            if (!camera.frustum.boundsInFrustum(
                    tempLightScreenPos.x,
                    tempLightScreenPos.y,
                    0f,
                    radiusX,
                    radiusY,
                    0f
                )
            ) {
                return@forEachPointLight
            }

            val candidate = lightCandidatePool.obtain()
            candidate.entityId = entity.id
            candidate.x = tempLightScreenPos.x
            candidate.y = tempLightScreenPos.y
            candidate.radiusX = radiusX
            candidate.radiusY = radiusY
            candidate.r = pointLight.color.r
            candidate.g = pointLight.color.g
            candidate.b = pointLight.color.b
            candidate.intensity = pointLight.intensity
            candidate.distanceToCamera2 = tempLightScreenPos.dst2(camera.position.x, camera.position.y)
            lightCandidates.add(candidate)
        }

        lightCandidates.sort(LIGHT_CANDIDATE_COMPARATOR)
        val lightCount = minOf(lightCandidates.size, MAX_PIXEL_LIGHTS)

        for (i in 0..<lightCount) {
            val candidate = lightCandidates[i]
            val offset = i * 4

            lightPositionRadii[offset] = candidate.x
            lightPositionRadii[offset + 1] = candidate.y
            lightPositionRadii[offset + 2] = candidate.radiusX
            lightPositionRadii[offset + 3] = candidate.radiusY

            lightColorIntensities[offset] = candidate.r
            lightColorIntensities[offset + 1] = candidate.g
            lightColorIntensities[offset + 2] = candidate.b
            lightColorIntensities[offset + 3] = candidate.intensity
        }

        shader.setUniformf(
            "u_ambientLight",
            lighting.ambientLight.r,
            lighting.ambientLight.g,
            lighting.ambientLight.b
        )
        shader.setUniformi("u_lightCount", lightCount)
        if (lightCount > 0) {
            shader.setUniform4fv("u_lightPositionRadii[0]", lightPositionRadii, 0, lightCount * 4)
            shader.setUniform4fv("u_lightColorIntensity[0]", lightColorIntensities, 0, lightCount * 4)
        }
    }

    private fun addRenderable(entity: Entity) {
        val renderable = getRenderable(entity) ?: return
        if (!inFrustum(renderable)) {
            pool.free(renderable)
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

    private fun draw(
        renderable: EntityRenderable,
        batch: PolygonSpriteBatch,
        lightingShader: ShaderProgram?
    ) {
        if (renderable.visited) {
            return
        }
        renderable.visited = true

        for (data in renderable.behind) {
            draw(data, batch, lightingShader)
        }

        occlusion.secondPass(renderable)

        if (renderable.alpha == 0f) {
            return
        }

        val color = Color(batch.color.r, batch.color.g, batch.color.b, renderable.alpha)
        val fillColor = renderable.fillColor
        val tintColor = renderable.tintColor

        var shaderChanged = false

        // Fill used as an intentionally unlit hit flash that overrides both tint and lighting.
        // The alpha is therefore always set to 1f.
        if (fillColor != null) {
            batch.shader = assets.shaders["color"]
            batch.shader.setUniformf(
                "u_color",
                fillColor.r,
                fillColor.g,
                fillColor.b,
                1f
            )
            shaderChanged = true
        } else if (renderable.entity!!.has<FullBright>()) {
            batch.shader = null
            shaderChanged = true
        } else {
            if (tintColor != null) {
                color.r = tintColor.r
                color.g = tintColor.g
                color.b = tintColor.b
            }
        }

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

            // Changing shaders flushes the batch, so only switch for intentionally unlit entities.
            if (shaderChanged) {
                batch.shader = lightingShader
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

        val bounds = collisionBoxes[entity] ?: Box.fromMinMax(worldPos, worldPos)

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

    private class LightCandidate {
        var entityId: String = ""
        var x: Float = 0f
        var y: Float = 0f
        var radiusX: Float = 0f
        var radiusY: Float = 0f
        var r: Float = 0f
        var g: Float = 0f
        var b: Float = 0f
        var intensity: Float = 0f
        var distanceToCamera2: Float = 0f
    }

    companion object {
        private val LIGHT_CANDIDATE_COMPARATOR = Comparator<LightCandidate> { a, b ->
            val distanceComparison = a.distanceToCamera2.compareTo(b.distanceToCamera2)
            if (distanceComparison != 0) {
                distanceComparison
            } else {
                a.entityId.compareTo(b.entityId)
            }
        }
    }
}
