package bke.iso.engine

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.math.Location
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entities
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("lamp")
class Lamp : Component

private data class LightSource(
    val location: Location,
    val intensity: Float,
    val falloff: Float
)

class Lighting(private val world: World) : EngineModule() {
    override val moduleName: String = "lighting"
    override val updateWhileLoading: Boolean = false
    override val profilingEnabled: Boolean = true

    private val lightSources = Array<LightSource>()
    private val lightMap = ObjectMap<Location, Float>()

    private val ambientLight = 0.25f

    override fun handleEvent(event: Event) {
        if (event is Entities.CreatedEvent) {
            if (event.entity.has<Lamp>()) {
                lightSources.add(
                    LightSource(
                        Location(pos = event.entity.pos),
                        intensity = 0.5f,
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

    fun mergeLights(lightMaps: List<ObjectMap<Location, Float>>): ObjectMap<Location, Float> {
        val merged = ObjectMap<Location, Float>()
        for (map in lightMaps) {
            for (entry in map) {
                val pos = entry.key
                val light = entry.value
                merged.put(pos, maxOf(merged[pos] ?: 0f, light))
            }
        }
        return merged
    }

    private fun floodFillLight(source: LightSource): ObjectMap<Location, Float> {
        val lightMap = ObjectMap<Location, Float>()
        val queue = ArrayDeque<Pair<Location, Float>>()

        lightMap.put(source.location, source.intensity)
        queue.add(source.location to source.intensity)

        while (queue.isNotEmpty()) {
            val (pos, currentLight) = queue.removeFirst()

            for (neighbor in getNeighbors(pos)) {
                val newLight = currentLight - source.falloff
                if (newLight > 0f && lightMap.get(neighbor, 0f) < newLight) {
                    lightMap.put(neighbor, newLight)
                    queue.add(neighbor to newLight)
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
        val lightValue = lightMap.get(Location(entity.x, entity.y, entity.z), 0f)
        r += lightValue
        g += lightValue
        b += lightValue
        return Triple(r, g, b)
    }
}
