package bke.iso.engine.pathfinding

import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Events
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class PathfindingTest : StringSpec({

    fun createWorld(collisionBoxes: CollisionBoxes): World {
        val events = Events { event -> collisionBoxes.handleEvent(event) }
        return World(events, collisionBoxes)
    }

    "coordinate conversion should round trip between world and recast axes" {
        val pathfinding = Pathfinding(CollisionBoxes())
        val worldPos = Vector3(3f, 4f, 2f)

        pathfinding.toWorld(pathfinding.toRecast(worldPos)).shouldBe(worldPos)
    }

    "pathing component should serialize without conflicting with the component type discriminator" {
        val serializer = Serializer()
        val components: List<Component> = listOf(Pathing(PathingType.BLOCKER))

        val json = serializer.write(components)
        val decoded = serializer.read<List<Component>>(json)

        json.contains("pathingType").shouldBe(true)
        (decoded.single() as Pathing).type.shouldBe(PathingType.BLOCKER)
    }

    "pathing inference should use tiles and zero-height colliders as walkable and other colliders as blockers" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val pathfinding = Pathfinding(collisionBoxes)

        val tile = world.entities.create(
            id = "tile",
            x = 0f,
            y = 0f,
            z = 0f,
            Tile(),
            Collider(size = Vector3(1f, 1f, 0f))
        )
        val blocker = world.entities.create(
            id = "blocker",
            x = 1f,
            y = 0f,
            z = 0f,
            Collider(size = Vector3(1f, 1f, 1f))
        )
        val ignored = world.entities.create(
            id = "ignored",
            x = 2f,
            y = 0f,
            z = 0f,
            Pathing(PathingType.IGNORE),
            Collider(size = Vector3(1f, 1f, 1f))
        )

        pathfinding.resolvePathingType(tile, collisionBoxes[tile]!!).shouldBe(PathingType.WALKABLE)
        pathfinding.resolvePathingType(blocker, collisionBoxes[blocker]!!).shouldBe(PathingType.BLOCKER)
        pathfinding.resolvePathingType(ignored, collisionBoxes[ignored]!!).shouldBe(PathingType.IGNORE)
    }

    "geometry extraction should create walkable top-face triangles and blocker volumes" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val pathfinding = Pathfinding(collisionBoxes)

        world.entities.create(
            id = "floor",
            x = 0f,
            y = 0f,
            z = 0f,
            Tile(),
            Collider(size = Vector3(2f, 2f, 0f))
        )
        world.entities.create(
            id = "blocker",
            x = 0.5f,
            y = 0.5f,
            z = 0f,
            Collider(size = Vector3(1f, 1f, 1f))
        )

        val geometry = pathfinding.extractGeometry(world)

        // One rectangular top face is stored as four 3D vertices: 4 vertices * 3 floats.
        geometry.vertices.size.shouldBe(12)
        // Recast consumes triangles, so that same rectangle becomes two triangles: 2 tris * 3 indices.
        geometry.triangles.size.shouldBe(6)
        // The blocker is not triangulated as a surface; it becomes one convex carving volume.
        geometry.convexVolumes.size.shouldBe(1)
        geometry.stats.walkableCount.shouldBe(1)
        geometry.stats.blockerCount.shouldBe(1)
        geometry.stats.triangleCount.shouldBe(2)
        geometry.stats.convexVolumeCount.shouldBe(1)
    }

    "generate navmesh should return polygons and debug lines for a small world" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val pathfinding = Pathfinding(collisionBoxes)

        world.entities.create(
            id = "floor",
            x = 0f,
            y = 0f,
            z = 0f,
            Tile(),
            Collider(size = Vector3(5f, 5f, 0f))
        )
        world.entities.create(
            id = "blocker",
            x = 2f,
            y = 2f,
            z = 0f,
            Collider(size = Vector3(1f, 1f, 1f))
        )

        val result = pathfinding.generateNavMesh(world)

        result.success.shouldBeTrue()
        result.stats.polygonCount.shouldBeGreaterThan(0)
        result.debugLines.shouldNotBeEmpty()
    }
})
