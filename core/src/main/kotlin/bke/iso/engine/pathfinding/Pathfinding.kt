package bke.iso.engine.pathfinding

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color

const val NAVMESH_DEBUG_CATEGORY = "navmesh"

class Pathfinding(collisionBoxes: CollisionBoxes) {

    private val config = PathfindingConfig()
    private val generator = NavMeshGenerator(collisionBoxes)

    var lastResult: NavMeshGenerationResult? = null
        private set

    fun generateNavMesh(world: World): NavMeshGenerationResult {
        val result = generator.generateNavMesh(world, config)
        lastResult = result
        return result
    }

    /**
     * Replays the latest generated navmesh edges into the debug renderer each frame.
     */
    fun drawDebug(debugRenderer: DebugRenderer) {
        val result = lastResult ?: return
        val category = debugRenderer.category(NAVMESH_DEBUG_CATEGORY)
        for (line in result.debugLines) {
            category.addLine(line.start, line.end, 1f, Color.CYAN)
        }
    }
}
