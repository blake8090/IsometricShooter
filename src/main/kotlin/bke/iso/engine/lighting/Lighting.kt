package bke.iso.engine.lighting

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.math.Location
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.event.EntityComponentAdded
import bke.iso.engine.world.event.EntityComponentRemoved
import bke.iso.engine.world.event.EntityCreated
import bke.iso.engine.world.event.EntityDeleted
import bke.iso.engine.world.event.EntityMoved
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap

private typealias LightMap = ObjectMap<Location, Triple<Float, Float, Float>>

private data class LightSource(
    val intensity: Float = 0f,
    val falloff: Float = 0f,
    val r: Float = 0f,
    val g: Float = 0f,
    val b: Float = 0f
) : Component

private data class DynamicLight(
    val source: LightSource,
    val lightMap: LightMap = LightMap()
)

class Lighting(private val world: World) : EngineModule() {
    override val moduleName: String = "lighting"
    override val updateWhileLoading: Boolean = true
    override val profilingEnabled: Boolean = true

    var ambientLight: Color = Color.WHITE

    private val dynamicLights = ObjectMap<Entity, DynamicLight>()

    override fun handleEvent(event: Event) {
        when (event) {
            is EntityCreated -> onEntityCreated(event.entity)

            is EntityDeleted -> {
                dynamicLights.remove(event.entity)
            }

            is EntityMoved -> {
                // TODO: track light source position and only update if light source grid location has changed
                if (event.entity.has<PointLight>()) {
                    recalculateDynamicLightMap(event.entity)
                }
            }

            is EntityComponentAdded -> {
                if (event.component is PointLight) {
                    update(event.entity)
                }
            }

            is EntityComponentRemoved -> {
                if (event.component is PointLight) {
                    update(event.entity)
                }
            }
        }
    }

    private fun onEntityCreated(entity: Entity) {
        val light = entity.get<PointLight>() ?: return

        val source =
            LightSource(
                light.intensity,
                light.falloff,
                light.color.r,
                light.color.g,
                light.color.b
            )

        dynamicLights.put(entity, DynamicLight(source))
        recalculateDynamicLightMap(entity)
    }

    private fun recalculateDynamicLightMap(entity: Entity) {
        val dynamicLight = dynamicLights[entity] ?: return
        val location = Location(entity.x, entity.y, entity.z)

        dynamicLight.lightMap.clear()
        dynamicLight.lightMap.putAll(floodFillLight(location, dynamicLight.source))
    }

    private fun floodFillLight(location: Location, source: LightSource): LightMap {
        val intensityMap = ObjectMap<Location, Float>()
        floodFillRecursive(location, source, source.intensity, intensityMap)

        val colorMap = LightMap()
        for (entry in intensityMap) {
            val pos = entry.key
            val intensity = entry.value

            val r = source.intensity * (source.r * intensity).coerceAtLeast(0f)
            val g = source.intensity * (source.g * intensity).coerceAtLeast(0f)
            val b = source.intensity * (source.b * intensity).coerceAtLeast(0f)
            colorMap.put(pos, Triple(r, g, b))
        }
        return colorMap
    }

    private fun floodFillRecursive(
        location: Location,
        source: LightSource,
        intensity: Float,
        intensityMap: ObjectMap<Location, Float>
    ) {
        // if intensity is too low, or we've already visited this location with a higher intensity, stop
        if (intensity <= 0f || intensityMap.get(location, 0f) >= intensity) {
            return
        }

        intensityMap.put(location, intensity)
        val nextIntensity = intensity - source.falloff

        // no more light to propagate
        if (nextIntensity <= 0f) {
            return
        }

        for (neighbor in getNeighbors(location)) {
            floodFillRecursive(neighbor, source, nextIntensity, intensityMap)
        }
    }

    private fun getNeighbors(pos: Location): Array<Location> {
        return Array.with(
            Location(pos.x + 1, pos.y, pos.z),
            Location(pos.x - 1, pos.y, pos.z),
            Location(pos.x, pos.y + 1, pos.z),
            Location(pos.x, pos.y - 1, pos.z),
            Location(pos.x, pos.y, pos.z + 1),
            Location(pos.x, pos.y, pos.z - 1)
        )
    }

    fun clear() {
        dynamicLights.clear()
    }

    fun update(entity: Entity) {
        val light = entity.get<PointLight>() ?: return

        val source =
            LightSource(
                light.intensity,
                light.falloff,
                light.color.r,
                light.color.g,
                light.color.b
            )

        dynamicLights.put(entity, DynamicLight(source))
        recalculateDynamicLightMap(entity)
    }

    fun getColor(entity: Entity): Triple<Float, Float, Float> {
        if (entity.has<FullBright>()) {
            return Triple(1f, 1f, 1f)
        }

        var r = ambientLight.r
        var g = ambientLight.g
        var b = ambientLight.b

        val location = Location(entity.x, entity.y, entity.z)
        var maxR = 0f
        var maxG = 0f
        var maxB = 0f

        for (dynamicLight in dynamicLights.values()) {
            val (currentR, currentG, currentB) = dynamicLight.lightMap[location] ?: continue

            if (currentR > maxR) {
                maxR = currentR
            }
            if (currentG > maxG) {
                maxG = currentG
            }
            if (currentB > maxB) {
                maxB = currentB
            }
        }

        r += maxR
        g += maxG
        b += maxB

        return Triple(
            r.coerceAtMost(1f),
            g.coerceAtMost(1f),
            b.coerceAtMost(1f)
        )
    }
}
