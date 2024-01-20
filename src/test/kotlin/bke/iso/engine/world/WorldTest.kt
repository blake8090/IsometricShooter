package bke.iso.engine.world

import bke.iso.engine.Game
import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk

class WorldTest : StringSpec({

    val events = mockk<Game.Events>()
    every { events.fire(any()) } returns mockk()

    "should return all objects in area" {
        val world = World(events)
        val actor = world.actors.create("actor", 0f, 0f, 0f)
        val actor2 = world.actors.create("actor2", 1f, 0f, 0f)
        world.actors.create("actor3", 2f, 0f, 0f)

        val area = Box(
            pos = Vector3(0.5f, 0.5f, 0f),
            size = Vector3(1f, 1f, 1f)
        )

        world.getObjectsInArea(area).shouldContainExactlyInAnyOrder(actor, actor2)
    }

    "should return all objects in area with negative positions" {
        val world = World(events)
        val actor = world.actors.create("actor", 0f, 0f, 0f)
        val actor2 = world.actors.create("actor2", 0f, -1f, 0f)
        world.actors.create("actor3", -0.5f, -0.5f, 0f)

        val area = Box(
            pos = Vector3(0.5f, -0.5f, 0f),
            size = Vector3(0.5f, 0.5f, 0.5f)
        )

        world.getObjectsInArea(area).shouldContainExactlyInAnyOrder(actor, actor2)
    }
})
