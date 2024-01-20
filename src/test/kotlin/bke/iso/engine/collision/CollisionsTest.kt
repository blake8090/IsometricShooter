package bke.iso.engine.collision

import bke.iso.engine.Game
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.render.DebugRenderer
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class CollisionsTest : StringSpec({

    val events = mockk<Game.Events>()
    every { events.fire(any()) } returns mockk()

    "when actor stands on top of tile, given precision error, should return collision" {
        val renderer = mockk<Renderer>()
        val debugRenderer = mockk<DebugRenderer>()
        every { debugRenderer.addBox(any<Box>(), any<Float>(), any<Color>()) } returns Unit
        every { renderer.debug } returns debugRenderer
        val world = World(events)

        val collisions = Collisions(renderer, world)

        val actor = world.actors.create(
            Vector3(0f, 0f, 4f),
            Collider(
                size = Vector3(0.4f, 0.4f, 1.4f),
                offset = Vector3(-0.2f, -0.2f, 0.0f)
            )
        )
        world.setTile(Location(0, 0, 4), Sprite())

        val delta = Vector3(0f, 0f, -1f)
        val predictedCollisions = collisions.predictCollisions(actor, delta)

        predictedCollisions.size shouldBe 1
        val collision = predictedCollisions.first()
        Tile::class.isInstance(collision.obj) shouldBe true
        collision.side shouldBe CollisionSide.TOP
    }
})
