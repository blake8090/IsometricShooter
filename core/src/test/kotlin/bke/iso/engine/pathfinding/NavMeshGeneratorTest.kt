package bke.iso.engine.pathfinding

import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Events
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class NavMeshGeneratorTest : StringSpec({
    fun createProfile(name: String = "default", radius: Float = 0.2f): PathfindingProfile =
        PathfindingProfile(
            name = name,
            height = 1.5f,
            radius = radius,
            maxClimb = 0.5f,
            maxSlope = 45f
        )

    fun createConfig(profile: PathfindingProfile = createProfile()): PathfindingConfig =
        PathfindingConfig(
            profiles = listOf(profile)
        )

    fun createWorld(collisionBoxes: CollisionBoxes): World {
        val events = Events { event -> collisionBoxes.handleEvent(event) }
        return World(events, collisionBoxes)
    }

    "coordinate conversion should round trip between world and recast axes" {
        val generator = NavMeshGenerator(CollisionBoxes())
        val worldPos = Vector3(3f, 4f, 2f)

        generator.toWorld(generator.toRecast(worldPos)).shouldBe(worldPos)
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
        val generator = NavMeshGenerator(collisionBoxes)

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

        generator.resolvePathingType(tile, collisionBoxes[tile]!!).shouldBe(PathingType.WALKABLE)
        generator.resolvePathingType(blocker, collisionBoxes[blocker]!!).shouldBe(PathingType.BLOCKER)
        generator.resolvePathingType(ignored, collisionBoxes[ignored]!!).shouldBe(PathingType.IGNORE)
    }

    "geometry extraction should create walkable top-face triangles and blocker volumes" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val generator = NavMeshGenerator(collisionBoxes)

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

        val config = createConfig()
        val profile = config.profiles.single()
        val geometry = generator.extractGeometry(world, config, profile)

        // One rectangular top face is stored as four 3D vertices: 4 vertices * 3 floats.
        geometry.vertices.size.shouldBe(12)
        // Recast consumes triangles, so that same rectangle becomes two triangles: 2 tris * 3 indices.
        geometry.triangles.size.shouldBe(6)
        // The blocker is not triangulated as a surface; it becomes one convex carving volume.
        geometry.convexVolumes.size.shouldBe(1)
        val padding = profile.radius + config.cellSize
        geometry.convexVolumes.single().verts.toList().shouldBe(
            listOf(
                0.5f - padding, 0f, 0.5f - padding,
                1.5f + padding, 0f, 0.5f - padding,
                1.5f + padding, 0f, 1.5f + padding,
                0.5f - padding, 0f, 1.5f + padding
            )
        )
        geometry.stats.walkableCount.shouldBe(1)
        geometry.stats.blockerCount.shouldBe(1)
        geometry.stats.triangleCount.shouldBe(2)
        geometry.stats.convexVolumeCount.shouldBe(1)
    }

    "blocker volume padding should grow with profile radius" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val generator = NavMeshGenerator(collisionBoxes)
        val smallProfile = createProfile(name = "small", radius = 0.2f)
        val largeProfile = createProfile(name = "large", radius = 1f)

        world.entities.create(
            id = "floor",
            x = 0f,
            y = 0f,
            z = 0f,
            Tile(),
            Collider(size = Vector3(10f, 10f, 0f))
        )
        world.entities.create(
            id = "blocker",
            x = 4.5f,
            y = 4.5f,
            z = 0f,
            Collider(size = Vector3(1f, 1f, 1f))
        )

        val smallConfig = createConfig(smallProfile)
        val largeConfig = createConfig(largeProfile)
        val smallGeometry = generator.extractGeometry(world, smallConfig, smallProfile)
        val largeGeometry = generator.extractGeometry(world, largeConfig, largeProfile)
        val smallPadding = smallProfile.radius + smallConfig.cellSize
        val largePadding = largeProfile.radius + largeConfig.cellSize

        smallGeometry.convexVolumes.single().verts[0].shouldBe(4.5f - smallPadding)
        largeGeometry.convexVolumes.single().verts[0].shouldBe(4.5f - largePadding)
        largeGeometry.convexVolumes.single().verts[0].shouldBeLessThan(smallGeometry.convexVolumes.single().verts[0])

        generator.generateNavMesh(world, smallConfig, smallProfile).success.shouldBeTrue()
        generator.generateNavMesh(world, largeConfig, largeProfile).success.shouldBeTrue()
    }

    "generate navmesh should return polygons and debug lines for a small world" {
        val collisionBoxes = CollisionBoxes()
        val world = createWorld(collisionBoxes)
        val generator = NavMeshGenerator(collisionBoxes)
        val config = createConfig()

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

        val result = generator.generateNavMesh(world, config, config.profiles.single())

        result.success.shouldBeTrue()
        result.stats.polygonCount.shouldBeGreaterThan(0)
        result.debugLines.shouldNotBeEmpty()
    }
})
