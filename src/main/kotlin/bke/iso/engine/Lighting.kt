package bke.iso.engine

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.math.Location
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entities
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("lamp")
class Lamp : Component

private data class LightSource(
    val location: Location,
    val color: Color,
    val falloff: Float
)

class Lighting(private val world: World) : EngineModule() {
    override val moduleName: String = "lighting"
    override val updateWhileLoading: Boolean = false
    override val profilingEnabled: Boolean = true

    private val lightSources = Array<LightSource>()
    private val lightMap = ObjectMap<Location, Color>()

    private val ambientLight = 0.25f

    override fun handleEvent(event: Event) {
        if (event is Entities.CreatedEvent) {
            if (event.entity.has<Lamp>()) {
                lightSources.add(
                    LightSource(
                        location = Location(pos = event.entity.pos),
                        color = Color(0.631f, 1f, 0.988f, 1f),
                        falloff = 0.11f
                    )
                )
                recalculateLightMap()
            }
        }
    }

    private fun recalculateLightMap() {
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
        val lightMap = ObjectMap<Location, Color>()
        val queue = ArrayDeque<Pair<Location, Color>>()

        val initialColor = source.color.cpy()
        lightMap.put(source.location, initialColor)
        queue.add(source.location to initialColor)

        while (queue.isNotEmpty()) {
            val (pos, currentColor) = queue.removeFirst()

            for (neighbor in getNeighbors(pos)) {
                val newColor = Color(
                    (currentColor.r - source.falloff).coerceAtLeast(0f),
                    (currentColor.g - source.falloff).coerceAtLeast(0f),
                    (currentColor.b - source.falloff).coerceAtLeast(0f),
                    currentColor.a
                )

                val hasAnyLight = newColor.r > 0f || newColor.g > 0f || newColor.b > 0f
                if (!hasAnyLight) {
                    continue
                }

                val existing = lightMap[neighbor]
                val improved = existing == null ||
                        newColor.r > existing.r ||
                        newColor.g > existing.g ||
                        newColor.b > existing.b

                if (improved) {
                    lightMap.put(neighbor, newColor)
                    queue.add(neighbor to newColor)
                }
            }
        }

        return lightMap
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
