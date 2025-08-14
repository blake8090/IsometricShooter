package bke.iso.engine

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.math.Location
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.event.EntityCreated
import bke.iso.engine.world.event.EntityDeleted
import bke.iso.engine.world.event.EntityGridLocationChanged
import bke.iso.game.entity.player.Player
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO: think of a better name for this, and add editor support
@Serializable
@SerialName("lamp")
class Lamp : Component

class Lighting(private val world: World) : EngineModule() {
    override val moduleName: String = "lighting"
    override val updateWhileLoading: Boolean = true
    override val profilingEnabled: Boolean = true

    private val log = KotlinLogging.logger { }

    private var lightingDirty = false

    private val lightSources = Array<LightSource>()
    private val lightMap = ObjectMap<Location, Color>()

    private val ambientLight = 0.06f

    override fun handleEvent(event: Event) {
        when (event) {
            is EntityCreated -> {
                if (event.entity.has<Lamp>()) {
                    log.debug { "Entity ${event.entity} created - recalculating light map" }
                    lightingDirty = true
                }
            }

            is EntityDeleted -> {
                if (event.entity.has<Lamp>()) {
                    log.debug { "Entity ${event.entity} created - recalculating light map" }
                    lightingDirty = true
                }
            }

            is EntityGridLocationChanged -> {
                if (event.entity.has<Lamp>()) {
                    log.debug { "Entity ${event.entity} grid location changed - recalculating light map" }
                    lightingDirty = true
                }
            }
        }
    }

    override fun update(deltaTime: Float) {
        if (lightingDirty) {

            recalculateLightMap()
            lightingDirty = false
        }
    }

    private fun recalculateLightMap() {
        lightSources.clear()
        world.entities.each<Lamp> { entity, lamp ->
            val lightSource =
                if (entity.has<Player>()) {
                    LightSource(
                        location = Location(pos = entity.pos),
                        color = Color(1f, 1f, 1f, 1f),
                        intensity = 0.1f,
                        falloff = 0.33f
                    )
                } else {
                    LightSource(
                        location = Location(pos = entity.pos),
                        color = Color(1f, 1f, 0.25f, 1f),
                        intensity = 0.8f,
                        falloff = 0.11f
                    )
                }
            lightSources.add(lightSource)
        }

        val lightMaps = lightSources
            .map(::floodFillLight)
            .toList()

        lightMap.clear()
        lightMap.putAll(mergeLights(lightMaps))
    }

    fun mergeLights(lightMaps: List<ObjectMap<Location, Color>>): ObjectMap<Location, Color> {
        val merged = ObjectMap<Location, Color>()
        for (map in lightMaps) {
            for (entry in map) {
                val pos = entry.key
                val light = entry.value
                val existing = merged[pos]
                if (existing == null) {
                    merged.put(pos, light.cpy())
                } else {
                    merged.put(
                        pos,
                        Color(
                            maxOf(existing.r, light.r),
                            maxOf(existing.g, light.g),
                            maxOf(existing.b, light.b),
                            maxOf(existing.a, light.a)
                        )
                    )
                }
            }
        }
        return merged
    }

    private fun floodFillLight(source: LightSource): ObjectMap<Location, Color> {
        // propagate a scalar intensity to preserve the color ratio
        val intensityMap = ObjectMap<Location, Float>()
        val queue = ArrayDeque<Pair<Location, Float>>()

        intensityMap.put(source.location, 1f)
        queue.add(source.location to 1f)

        while (queue.isNotEmpty()) {
            val (pos, currentIntensity) = queue.removeFirst()

            for (neighbor in getNeighbors(pos)) {
                val newIntensity = currentIntensity - source.falloff
                if (newIntensity > 0f && intensityMap.get(neighbor, 0f) < newIntensity) {
                    intensityMap.put(neighbor, newIntensity)
                    queue.add(neighbor to newIntensity)
                }
            }
        }

        val colorMap = ObjectMap<Location, Color>()
        for (entry in intensityMap) {
            val pos = entry.key
            val intensity = entry.value
            colorMap.put(
                pos,
                Color(
                    source.intensity * (source.color.r * intensity).coerceAtLeast(0f),
                    source.intensity * (source.color.g * intensity).coerceAtLeast(0f),
                    source.intensity * (source.color.b * intensity).coerceAtLeast(0f),
                    source.color.a
                )
            )
        }

        return colorMap
    }

    fun getNeighbors(pos: Location): Array<Location> {
        return Array.with(
            Location(pos.x + 1, pos.y, pos.z),
            Location(pos.x - 1, pos.y, pos.z),
            Location(pos.x, pos.y + 1, pos.z),
            Location(pos.x, pos.y - 1, pos.z),
            Location(pos.x, pos.y, pos.z + 1),
            Location(pos.x, pos.y, pos.z - 1)
        )
    }

    fun getColor(entity: Entity): Triple<Float, Float, Float> {
        var r = ambientLight
        var g = ambientLight
        var b = ambientLight
        val color = lightMap[Location(entity.x, entity.y, entity.z)]
        if (color != null) {
            r += color.r
            g += color.g
            b += color.b
        }
        return Triple(
            r.coerceAtMost(1f),
            g.coerceAtMost(1f),
            b.coerceAtMost(1f)
        )
    }
}

// TODO: store r, g, and b directly instead of using Color, and add brightness
private data class LightSource(
    val location: Location,
    val color: Color,
    val intensity: Float,
    val falloff: Float
)
