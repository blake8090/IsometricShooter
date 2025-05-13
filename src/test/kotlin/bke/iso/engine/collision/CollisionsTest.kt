package bke.iso.engine.collision

import bke.iso.engine.core.Events
import bke.iso.engine.render.debug.DebugCategory
import bke.iso.engine.render.debug.DebugRenderer
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class CollisionsTest : StringSpec({

    val events = mockk<Events>()
    every { events.fire(any()) } returns mockk()

    "when entity stands on top of tile, given precision error, should return collision" {
        val renderer = mockk<Renderer>()
        val debugRenderer = mockk<DebugRenderer>()
        every { debugRenderer.category(any<String>()) } returns DebugCategory()
        every { renderer.debug } returns debugRenderer

        val world = World(events)
        val collisions = Collisions(renderer, world)

        val tileEntity = world
            .entities
            .create(
                Vector3(0f, 0f, 4f),
                Tile()
            )

        val entity = world
            .entities
            .create(
                Vector3(0f, 0f, 4f),
                Collider(
                    size = Vector3(0.4f, 0.4f, 1.4f),
                    offset = Vector3(-0.2f, -0.2f, 0.0f)
                )
            )

        val delta = Vector3(0f, 0f, -1f)
        val predictedCollisions = collisions.predictCollisions(entity, delta)

        predictedCollisions.size shouldBe 1
        val collision = predictedCollisions.first()
        collision.entity shouldBe tileEntity
        collision.side shouldBe CollisionSide.TOP
    }
})
