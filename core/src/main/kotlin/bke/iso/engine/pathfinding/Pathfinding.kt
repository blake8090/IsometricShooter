package bke.iso.engine.pathfinding

import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import org.recast4j.detour.DefaultQueryFilter
import org.recast4j.detour.NavMeshQuery
import kotlin.math.max

const val NAVMESH_DEBUG_CATEGORY = "navmesh"

private const val MAX_STRAIGHT_PATH_POINTS = 256

sealed class PathResult {
    data class Success(val waypoints: List<Vector3>) : PathResult()
    data class Failure(val reason: String) : PathResult()
}

class Pathfinding(
    private val assets: Assets,
    collisionBoxes: CollisionBoxes
) {
    private val log = KotlinLogging.logger {}

    private val generator = NavMeshGenerator(collisionBoxes)

    private val navMeshesByProfile = mutableMapOf<String, NavMeshGenerationResult>()

    fun generateNavMeshes(world: World) {
        navMeshesByProfile.clear()

        val config = assets.configs.get<PathfindingConfig>("pathfinding.cfg")

        for (profile in config.profiles) {
            val result = generator.generateNavMesh(world, config, profile)
            if (result.success) {
                navMeshesByProfile[profile.name] = result
            }
        }
    }

    fun getProfileNames(): List<String> =
        assets.configs.get<PathfindingConfig>("pathfinding.cfg")
            .profiles
            .map { profile -> profile.name }
            .toList()

    fun findPath(profile: String, start: Vector3, end: Vector3): PathResult {
        log.debug { "Pathfinding[$profile] finding path: start=$start, end=$end" }

        val result = navMeshesByProfile[profile]
        if (result == null) {
            log.warn { "Pathfinding[$profile] failed: no generated navmesh" }
            return PathResult.Failure("No navmesh generated for profile '$profile'")
        }

        val navMesh = result.navMesh
        if (navMesh == null) {
            log.warn { "Pathfinding[$profile] failed: generated result has no navmesh" }
            return PathResult.Failure("Generated navmesh for profile '$profile' is missing")
        }

        val config = assets.configs.get<PathfindingConfig>("pathfinding.cfg")
        val pathfindingProfile = findProfile(config, profile)
        if (pathfindingProfile == null) {
            log.warn { "Pathfinding[$profile] failed: profile is missing from config" }
            return PathResult.Failure("Pathfinding profile '$profile' is missing from config")
        }

        val query = NavMeshQuery(navMesh)
        val filter = DefaultQueryFilter()
        val extents = nearestPolyExtents(config, pathfindingProfile)
        val startRecast = toRecast(start)
        val endRecast = toRecast(end)

        val startNearest = query.findNearestPoly(startRecast, extents, filter)
        if (startNearest.failed() || startNearest.result.nearestRef == 0L) {
            log.warn { "Pathfinding[$profile] failed: no nearest start polygon" }
            return PathResult.Failure("No nearest start polygon found for profile '$profile'")
        }

        val endNearest = query.findNearestPoly(endRecast, extents, filter)
        if (endNearest.failed() || endNearest.result.nearestRef == 0L) {
            log.warn { "Pathfinding[$profile] failed: no nearest end polygon" }
            return PathResult.Failure("No nearest end polygon found for profile '$profile'")
        }

        val polyPath = query.findPath(
            /* startRef = */ startNearest.result.nearestRef,
            /* endRef = */ endNearest.result.nearestRef,
            /* startPos = */ startRecast,
            /* endPos = */ endRecast,
            /* filter = */ filter
        )
        if (polyPath.failed() || polyPath.result.isEmpty()) {
            log.warn { "Pathfinding[$profile] failed: no polygon path found" }
            return PathResult.Failure("No path found for profile '$profile'")
        }

        val straightPath = query.findStraightPath(
            /* startPos = */ startNearest.result.nearestPos,
            /* endPos = */ endNearest.result.nearestPos,
            /* path = */ polyPath.result,
            /* maxStraightPath = */ MAX_STRAIGHT_PATH_POINTS,
            /* options = */ 0
        )
        if (straightPath.failed() || straightPath.result.isEmpty()) {
            log.warn { "Pathfinding[$profile] failed: no straight path found" }
            return PathResult.Failure("No straight path found for profile '$profile'")
        }

        val waypoints = straightPath.result.map { waypoint -> toWorld(waypoint.pos) }
        log.debug { "Pathfinding[$profile] found path: waypoints=${waypoints.size}" }
        return PathResult.Success(waypoints)
    }

    fun drawNavMesh(profile: String, debugRenderer: DebugRenderer) {
        val result = navMeshesByProfile[profile] ?: return

        for (line in result.debugLines) {
            debugRenderer
                .category(NAVMESH_DEBUG_CATEGORY)
                .addLine(line.start, line.end, 1f, Color.CYAN)
        }
    }

    fun clear() {
        navMeshesByProfile.clear()
    }

    private fun findProfile(config: PathfindingConfig, profileName: String): PathfindingProfile? =
        config.profiles.firstOrNull { profile -> profile.name == profileName }

    private fun nearestPolyExtents(config: PathfindingConfig, profile: PathfindingProfile): FloatArray =
        floatArrayOf(
            max(profile.radius * 2f, config.cellSize * 2f),
            max(profile.height, config.cellHeight * 2f),
            max(profile.radius * 2f, config.cellSize * 2f)
        )

    private fun toRecast(worldPos: Vector3): FloatArray =
        floatArrayOf(worldPos.x, worldPos.z, worldPos.y)

    private fun toWorld(recastPos: FloatArray): Vector3 =
        Vector3(recastPos[0], recastPos[2], recastPos[1])
}
