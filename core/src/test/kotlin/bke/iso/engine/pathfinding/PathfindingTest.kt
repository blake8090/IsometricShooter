package bke.iso.engine.pathfinding

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.config.Config
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Events
import bke.iso.engine.os.Files
import bke.iso.engine.os.SystemInfo
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import java.io.File

class PathfindingTest : StringSpec({

    fun createConfig(): PathfindingConfig =
        PathfindingConfig(
            profiles = listOf(
                PathfindingProfile(
                    name = "default",
                    height = 1.5f,
                    radius = 0.2f,
                    maxClimb = 0.5f,
                    maxSlope = 45f
                )
            )
        )

    fun createAssets(config: PathfindingConfig): Assets {
        val assets = Assets(mockk<Files>(), mockk<SystemInfo>())
        val cache = TestConfigAssetCache()
        cache.add("pathfinding.cfg", config)
        assets.addCache(Config::class, cache)
        return assets
    }

    fun createWorld(collisionBoxes: CollisionBoxes): World {
        val events = Events { event -> collisionBoxes.handleEvent(event) }
        return World(events, collisionBoxes)
    }

    "find path should fail when navmesh has not been generated" {
        val collisionBoxes = CollisionBoxes()
        val pathfinding = Pathfinding(createAssets(createConfig()), collisionBoxes)

        val result = pathfinding.findPath(
            profile = "default",
            start = Vector3(0f, 0f, 0f),
            end = Vector3(1f, 1f, 0f)
        )

        result.shouldBe(PathResult.Failure("No navmesh generated for profile 'default'"))
    }

    "find path should fail for unknown profile" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val pathfinding = Pathfinding(createAssets(createConfig()), collisionBoxes)

        world.entities.create(
            id = "floor",
            x = 0f,
            y = 0f,
            z = 0f,
            Tile(),
            Collider(size = Vector3(5f, 5f, 0f))
        )

        pathfinding.generateNavMeshes(world)

        val result = pathfinding.findPath(
            profile = "unknown",
            start = Vector3(0.5f, 0.5f, 0f),
            end = Vector3(4f, 4f, 0f)
        )

        result.shouldBe(PathResult.Failure("No navmesh generated for profile 'unknown'"))
    }

    "find path should return waypoints for generated navmesh" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val pathfinding = Pathfinding(createAssets(createConfig()), collisionBoxes)

        world.entities.create(
            id = "floor",
            x = 0f,
            y = 0f,
            z = 0f,
            Tile(),
            Collider(size = Vector3(5f, 5f, 0f))
        )

        pathfinding.generateNavMeshes(world)

        val result = pathfinding.findPath(
            profile = "default",
            start = Vector3(0.5f, 0.5f, 0f),
            end = Vector3(4f, 4f, 0f)
        )

        (result is PathResult.Success).shouldBeTrue()
        result as PathResult.Success
        result.waypoints.shouldNotBeEmpty()
    }
})

private class TestConfigAssetCache : AssetCache<Config>() {
    override val extensions: Set<String> = setOf("cfg")

    override suspend fun load(file: File) {
    }

    fun add(name: String, config: Config) {
        store(File(name), name, config)
    }
}
