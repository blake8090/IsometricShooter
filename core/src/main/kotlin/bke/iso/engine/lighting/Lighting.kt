package bke.iso.engine.lighting

import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.math.Location
import bke.iso.engine.render.Occlude
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tile
import bke.iso.engine.world.event.EntityComponentAdded
import bke.iso.engine.world.event.EntityComponentRemoved
import bke.iso.engine.world.event.EntityCreated
import bke.iso.engine.world.event.EntityDeleted
import bke.iso.engine.world.event.EntityMoved
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
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

    // TODO: hack to avoid circular references.. or maybe this should be a pattern?
    lateinit var collisions: Collisions

    private val tempStart = Vector3()
    private val tempEnd = Vector3()

    private val dynamicLights = ObjectMap<Entity, DynamicLight>()


    // Cache for line-of-sight calculations
    private val lineOfSightCache = mutableMapOf<Pair<Location, Location>, Boolean>()
    private val lastLightSourcePositions = mutableMapOf<Entity, Location>()

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
        val blockedMap = mutableMapOf<Location, Boolean>()
        val visitedMap = mutableMapOf<Location, Boolean>()
        floodFillRecursive(location, location, source, source.intensity, intensityMap, blockedMap, visitedMap)

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
        start: Location,
        location: Location,
        source: LightSource,
        intensity: Float,
        intensityMap: ObjectMap<Location, Float>,
        blockedMap: MutableMap<Location, Boolean>,
        visitedMap: MutableMap<Location, Boolean>
    ) {
        // if intensity is too low, or we've already visited this location with a higher intensity, stop
        if (intensity <= 0f || intensityMap.get(location, 0f) >= intensity) {
            return
        }

        intensityMap.put(location, intensity)
        val nextIntensity = intensity - source.falloff
        visitedMap.put(location, true)

        // no more light to propagate
        if (nextIntensity <= 0f) {
            return
        }




        if (start != location) {
//            val blocked = blockedMap.getOrPut(location) {
//                collisions.checkLineCollisions(start.toVector3(), location.toVector3())
//                    .any { it.entity.has<Occlude>() }
//            }
            val blocked = blockedMap.getOrPut(location) { isLineOfSightBlocked(start, location) }

            if (blocked) {
                return
            }
        }



        for (neighbor in getNeighbors(location)) {
            floodFillRecursive(start, neighbor, source, nextIntensity, intensityMap, blockedMap, visitedMap)
        }
    }

//    private fun isLineOfSightBlocked(start: Location, end: Location): Boolean {
//        // Reuse Vector3 objects to avoid allocation
//        tempStart.set(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
//        tempEnd.set(end.x.toFloat(), end.y.toFloat(), end.z.toFloat())
//
//        // Use a more efficient collision check
//        return collisions.checkLineCollisions(tempStart, tempEnd)
//            .any { collision ->
//                // Cache the entity reference to avoid repeated lookups
//                val entity = collision.entity
//                entity.has<Occlude>()
//            }
//    }

    private fun isLineOfSightBlocked(start: Location, end: Location): Boolean {
        val key = if (start.x < end.x && start.y < end.y && start.z < end.z) start to end else end to start

        return lineOfSightCache.getOrPut(key) {
            // Only do the expensive collision check once per line
            // Very important to add 0.01f to the z value so the line doesn't get caught on its own tile
            val startVec = Vector3(start.x.toFloat(), start.y.toFloat(), start.z.toFloat() + 0.01f)
            val endVec = Vector3(end.x.toFloat(), end.y.toFloat(), end.z.toFloat() + 0.01f)

            collisions.checkLineCollisions(startVec, endVec)
                .any { it.entity.has<Occlude>() || it.entity.has<Tile>() }
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
