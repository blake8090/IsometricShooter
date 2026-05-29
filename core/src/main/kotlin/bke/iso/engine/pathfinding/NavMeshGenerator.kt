package bke.iso.engine.pathfinding

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.math.Box
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import org.recast4j.detour.MeshData
import org.recast4j.detour.NavMesh
import org.recast4j.detour.NavMeshBuilder
import org.recast4j.detour.NavMeshDataCreateParams
import org.recast4j.detour.Poly
import org.recast4j.recast.AreaModification
import org.recast4j.recast.ConvexVolume
import org.recast4j.recast.PolyMesh
import org.recast4j.recast.PolyMeshDetail
import org.recast4j.recast.RecastBuilder
import org.recast4j.recast.RecastBuilderConfig
import org.recast4j.recast.RecastConfig
import org.recast4j.recast.RecastConstants
import org.recast4j.recast.RecastConstants.PartitionType
import org.recast4j.recast.geom.InputGeomProvider
import org.recast4j.recast.geom.TriMesh
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

private const val WALKABLE_AREA = 1
private const val WALKABLE_FLAG = 1
private const val DEFAULT_VERTS_PER_POLY = 6
private const val DEFAULT_REGION_MIN_SIZE = 4
private const val DEFAULT_REGION_MERGE_SIZE = 20
private const val DEFAULT_EDGE_MAX_LEN = 12f
private const val DEFAULT_EDGE_MAX_ERROR = 1.3f
private const val DEFAULT_DETAIL_SAMPLE_DIST = 6f
private const val DEFAULT_DETAIL_SAMPLE_MAX_ERROR = 1f

data class NavMeshDebugLine(
    val start: Vector3,
    val end: Vector3
)

data class NavMeshSourceStats(
    val entityCount: Int = 0,
    val walkableCount: Int = 0,
    val blockerCount: Int = 0,
    val ignoredCount: Int = 0,
    val triangleCount: Int = 0,
    val convexVolumeCount: Int = 0,
    val polygonCount: Int = 0,
    val debugLineCount: Int = 0
)

data class NavMeshGenerationResult(
    val success: Boolean,
    val navMesh: NavMesh?,
    val meshData: MeshData?,
    val stats: NavMeshSourceStats,
    val debugLines: List<NavMeshDebugLine>
)

data class NavMeshSourceGeometry(
    val vertices: FloatArray,
    val triangles: IntArray,
    val convexVolumes: List<ConvexVolume>,
    val stats: NavMeshSourceStats
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NavMeshSourceGeometry) return false
        return vertices.contentEquals(other.vertices) &&
                triangles.contentEquals(other.triangles) &&
                convexVolumes == other.convexVolumes &&
                stats == other.stats
    }

    override fun hashCode(): Int {
        var result = vertices.contentHashCode()
        result = 31 * result + triangles.contentHashCode()
        result = 31 * result + convexVolumes.hashCode()
        result = 31 * result + stats.hashCode()
        return result
    }
}

class NavMeshGenerator(private val collisionBoxes: CollisionBoxes) {

    private val log = KotlinLogging.logger {}

    /**
     * Builds a complete Recast/Detour navmesh from the current world.
     */
    fun generateNavMesh(
        world: World,
        config: PathfindingConfig,
        profile: PathfindingProfile
    ): NavMeshGenerationResult {
        log.info { "Navmesh[${profile.name}] generating" }
        log.debug { "Navmesh[${profile.name}] config: $config" }

        lateinit var result: NavMeshGenerationResult
        val elapsedMillis = measureTimeMillis {
            result =
                try {
                    generate(world, config, profile)
                } catch (e: RuntimeException) {
                    log.error(e) { "Navmesh[${profile.name}] generation threw an exception" }
                    NavMeshGenerationResult(
                        success = false,
                        navMesh = null,
                        meshData = null,
                        stats = NavMeshSourceStats(),
                        debugLines = emptyList()
                    )
                }
        }

        if (result.success) {
            log.info {
                "Navmesh[${profile.name}] generated in ${elapsedMillis}ms"
            }
        } else {
            log.warn {
                "Navmesh[${profile.name}] generation failed in ${elapsedMillis}ms"
            }
        }

        return result
    }

    /**
     * Converts world entities into the raw triangle mesh and blocker volumes that Recast consumes.
     */
    internal fun extractGeometry(world: World, config: PathfindingConfig): NavMeshSourceGeometry {
        val vertices = mutableListOf<Float>()
        val triangles = mutableListOf<Int>()
        val volumes = mutableListOf<ConvexVolume>()

        var entityCount = 0
        var walkableCount = 0
        var blockerCount = 0
        var ignoredCount = 0

        for (entity in world.entities) {
            val box = collisionBoxes[entity]
            if (box == null) {
                ignoredCount++
                continue
            }

            entityCount++
            when (resolvePathingType(entity, box)) {
                PathingType.WALKABLE -> {
                    walkableCount++
                    addWalkableTopFace(box, vertices, triangles)
                }

                PathingType.BLOCKER -> {
                    blockerCount++
                    volumes.add(createBlockerVolume(box, config))
                }

                PathingType.IGNORE -> ignoredCount++
            }
        }

        val geometry = NavMeshSourceGeometry(
            vertices = vertices.toFloatArray(),
            triangles = triangles.toIntArray(),
            convexVolumes = volumes,
            stats = NavMeshSourceStats(
                entityCount = entityCount,
                walkableCount = walkableCount,
                blockerCount = blockerCount,
                ignoredCount = ignoredCount,
                triangleCount = triangles.size / 3,
                convexVolumeCount = volumes.size
            )
        )
        return geometry
    }

    /**
     * Chooses how an entity participates in pathfinding, using Pathing overrides before inferred defaults.
     */
    internal fun resolvePathingType(entity: Entity, box: Box): PathingType {
        val pathing = entity.get<Pathing>()
        if (pathing != null) {
            return pathing.type
        }
        return if (entity.has<Tile>() || box.size.z == 0f) {
            PathingType.WALKABLE
        } else {
            PathingType.BLOCKER
        }
    }

    /**
     * Converts from this game's z-up world coordinates to Recast's y-up coordinate system.
     */
    internal fun toRecast(worldPos: Vector3): FloatArray =
        floatArrayOf(worldPos.x, worldPos.z, worldPos.y)

    /**
     * Converts from Recast's y-up coordinate system back to this game's z-up world coordinates.
     */
    internal fun toWorld(recastPos: FloatArray): Vector3 =
        Vector3(recastPos[0], recastPos[2], recastPos[1])

    /**
     * Runs the Recast build, converts the result into a Detour NavMesh, and extracts debug edges.
     */
    private fun generate(
        world: World,
        config: PathfindingConfig,
        profile: PathfindingProfile
    ): NavMeshGenerationResult {
        val geometry = extractGeometry(world, config)
        if (geometry.triangles.isEmpty()) {
            log.warn { "Navmesh[${profile.name}] generation failed: No walkable geometry found" }
            return NavMeshGenerationResult(
                success = false,
                navMesh = null,
                meshData = null,
                stats = geometry.stats,
                debugLines = emptyList()
            )
        }
        log.info { "Navmesh[${profile.name}] source geometry extracted: ${formatStats(geometry.stats)}" }

        val geomProvider = WorldInputGeomProvider(
            geometry.vertices,
            geometry.triangles,
            geometry.convexVolumes
        )
        log.info {
            "Navmesh[${profile.name}] source bounds: min=${
                geomProvider.getMeshBoundsMin().contentToString()
            }, max=${geomProvider.getMeshBoundsMax().contentToString()}"
        }
        val recastConfig = RecastConfig(
            PartitionType.WATERSHED,
            config.cellSize,
            config.cellHeight,
            profile.height,
            profile.radius,
            profile.maxClimb,
            profile.maxSlope,
            DEFAULT_REGION_MIN_SIZE,
            DEFAULT_REGION_MERGE_SIZE,
            DEFAULT_EDGE_MAX_LEN,
            DEFAULT_EDGE_MAX_ERROR,
            DEFAULT_VERTS_PER_POLY,
            DEFAULT_DETAIL_SAMPLE_DIST,
            DEFAULT_DETAIL_SAMPLE_MAX_ERROR,
            AreaModification(WALKABLE_AREA)
        )

        val builderConfig = RecastBuilderConfig(
            recastConfig,
            geomProvider.getMeshBoundsMin(),
            geomProvider.getMeshBoundsMax()
        )
        val recastResult = RecastBuilder().build(geomProvider, builderConfig)
        val polyMesh = recastResult.mesh
        if (polyMesh.npolys == 0) {
            log.warn { "Recast generated zero navmesh polygons from ${formatStats(geometry.stats)}" }
            return NavMeshGenerationResult(
                success = false,
                navMesh = null,
                meshData = null,
                stats = geometry.stats,
                debugLines = emptyList()
            )
        }

        val meshData = createMeshData(polyMesh, recastResult.meshDetail, config, profile)
        if (meshData == null) {
            log.warn { "Detour did not create mesh data from ${polyMesh.npolys} Recast polygons" }
            return NavMeshGenerationResult(
                success = false,
                navMesh = null,
                meshData = null,
                stats = geometry.stats.copy(polygonCount = polyMesh.npolys),
                debugLines = emptyList()
            )
        }

        val navMesh = NavMesh(meshData, DEFAULT_VERTS_PER_POLY, 0)
        val debugLines = createDebugLines(meshData)
        val stats = geometry.stats.copy(
            polygonCount = polyMesh.npolys,
            debugLineCount = debugLines.size
        )
        return NavMeshGenerationResult(
            success = true,
            navMesh = navMesh,
            meshData = meshData,
            stats = stats,
            debugLines = debugLines
        )
    }

    private fun formatStats(stats: NavMeshSourceStats): String =
        "entities=${stats.entityCount}, walkable=${stats.walkableCount}, blockers=${stats.blockerCount}, " +
                "ignored=${stats.ignoredCount}, sourceTris=${stats.triangleCount}, volumes=${stats.convexVolumeCount}, " +
                "polys=${stats.polygonCount}, debugLines=${stats.debugLineCount}"

    /**
     * Adds two triangles for the top face of a box, which gives Recast a walkable surface.
     */
    private fun addWalkableTopFace(
        box: Box,
        vertices: MutableList<Float>,
        triangles: MutableList<Int>
    ) {
        val firstIndex = vertices.size / 3
        addVertex(Vector3(box.min.x, box.min.y, box.max.z), vertices)
        addVertex(Vector3(box.max.x, box.min.y, box.max.z), vertices)
        addVertex(Vector3(box.max.x, box.max.y, box.max.z), vertices)
        addVertex(Vector3(box.min.x, box.max.y, box.max.z), vertices)

        triangles.add(firstIndex)
        triangles.add(firstIndex + 2)
        triangles.add(firstIndex + 1)

        triangles.add(firstIndex)
        triangles.add(firstIndex + 3)
        triangles.add(firstIndex + 2)
    }

    /**
     * Appends one world-space point to the Recast vertex array after axis conversion.
     */
    private fun addVertex(worldPos: Vector3, vertices: MutableList<Float>) {
        val recastPos = toRecast(worldPos)
        vertices.add(recastPos[0])
        vertices.add(recastPos[1])
        vertices.add(recastPos[2])
    }

    /**
     * Creates a vertical footprint that tells Recast to remove walkable spans inside a blocker.
     */
    private fun createBlockerVolume(box: Box, config: PathfindingConfig): ConvexVolume {
        val volume = ConvexVolume()
        volume.verts = floatArrayOf(
            box.min.x, box.min.z, box.min.y,
            box.max.x, box.min.z, box.min.y,
            box.max.x, box.min.z, box.max.y,
            box.min.x, box.min.z, box.max.y
        )
        volume.hmin = box.min.z - config.cellHeight
        volume.hmax = box.max.z + config.cellHeight
        // RC_NULL_AREA is Recast's "not walkable" area id, so this carves the blocker out of the navmesh.
        volume.areaMod = AreaModification(RecastConstants.RC_NULL_AREA)
        return volume
    }

    /**
     * Packages Recast's polygon/detail meshes into Detour's runtime NavMesh data format.
     */
    private fun createMeshData(
        polyMesh: PolyMesh,
        detailMesh: PolyMeshDetail,
        config: PathfindingConfig,
        profile: PathfindingProfile
    ): MeshData? {
        for (i in 0 until polyMesh.npolys) {
            polyMesh.flags[i] = WALKABLE_FLAG
        }

        val params = NavMeshDataCreateParams()
        params.verts = polyMesh.verts
        params.vertCount = polyMesh.nverts
        params.polys = polyMesh.polys
        params.polyAreas = polyMesh.areas
        params.polyFlags = polyMesh.flags
        params.polyCount = polyMesh.npolys
        params.nvp = polyMesh.nvp
        params.detailMeshes = detailMesh.meshes
        params.detailVerts = detailMesh.verts
        params.detailVertsCount = detailMesh.nverts
        params.detailTris = detailMesh.tris
        params.detailTriCount = detailMesh.ntris
        params.walkableHeight = profile.height
        params.walkableRadius = profile.radius
        params.walkableClimb = profile.maxClimb
        params.bmin = polyMesh.bmin
        params.bmax = polyMesh.bmax
        params.cs = config.cellSize
        params.ch = config.cellHeight
        params.buildBvTree = true
        return NavMeshBuilder.createNavMeshData(params)
    }

    /**
     * Converts each Detour polygon edge into world-space lines for the existing isometric debug renderer.
     */
    private fun createDebugLines(meshData: MeshData): List<NavMeshDebugLine> {
        val lines = mutableListOf<NavMeshDebugLine>()
        for (poly in meshData.polys) {
            if (poly.type != Poly.DT_POLYTYPE_GROUND) {
                continue
            }

            for (i in 0 until poly.vertCount) {
                val next = (i + 1) % poly.vertCount
                val start = getWorldVertex(meshData, poly.verts[i])
                val end = getWorldVertex(meshData, poly.verts[next])
                lines.add(NavMeshDebugLine(start, end))
            }
        }
        return lines
    }

    /**
     * Looks up one Detour vertex and converts it back to world coordinates.
     */
    private fun getWorldVertex(meshData: MeshData, vertexIndex: Int): Vector3 {
        val index = vertexIndex * 3
        return toWorld(
            floatArrayOf(
                meshData.verts[index],
                meshData.verts[index + 1],
                meshData.verts[index + 2]
            )
        )
    }
}

private class WorldInputGeomProvider(
    vertices: FloatArray,
    triangles: IntArray,
    private val convexVolumes: List<ConvexVolume>
) : InputGeomProvider {

    private val mesh = TriMesh(vertices, triangles)
    private val boundsMin = FloatArray(3)
    private val boundsMax = FloatArray(3)

    init {
        require(vertices.size >= 3) { "Expected at least one vertex" }
        boundsMin[0] = vertices[0]
        boundsMin[1] = vertices[1]
        boundsMin[2] = vertices[2]
        boundsMax[0] = vertices[0]
        boundsMax[1] = vertices[1]
        boundsMax[2] = vertices[2]

        var index = 3
        while (index < vertices.size) {
            boundsMin[0] = min(boundsMin[0], vertices[index])
            boundsMin[1] = min(boundsMin[1], vertices[index + 1])
            boundsMin[2] = min(boundsMin[2], vertices[index + 2])
            boundsMax[0] = max(boundsMax[0], vertices[index])
            boundsMax[1] = max(boundsMax[1], vertices[index + 1])
            boundsMax[2] = max(boundsMax[2], vertices[index + 2])
            index += 3
        }

        if (boundsMin[1] == boundsMax[1]) {
            boundsMin[1] -= 0.1f
            boundsMax[1] += 0.1f
        }
    }

    override fun getMeshBoundsMin(): FloatArray =
        boundsMin

    override fun getMeshBoundsMax(): FloatArray =
        boundsMax

    override fun meshes(): Iterable<TriMesh> =
        listOf(mesh)

    override fun convexVolumes(): Iterable<ConvexVolume> =
        convexVolumes
}
