package bke.iso.engine.pathfinding

import bke.iso.engine.asset.Assets
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color

const val NAVMESH_DEBUG_CATEGORY = "navmesh"

class Pathfinding(
    private val assets: Assets,
    collisionBoxes: CollisionBoxes
) {
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
}
