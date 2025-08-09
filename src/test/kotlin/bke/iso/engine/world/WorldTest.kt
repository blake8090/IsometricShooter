package bke.iso.engine.world

import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.mockk.every
import io.mockk.mockk

class WorldTest : StringSpec({

    val events = mockk<Events>()
    every { events.fire(any()) } returns mockk()

    "should return all objects in area" {
        val world = World(events)
        val entity = world.entities.create("entity", 0f, 0f, 0f)
        val entity2 = world.entities.create("entity2", 1f, 0f, 0f)
        world.entities.create("entity3", 2f, 0f, 0f)

        val area = Box(
            pos = Vector3(0.5f, 0.5f, 0f),
            size = Vector3(1f, 1f, 1f)
        )

        world.entities.findAllIn(area).shouldContainAll(entity, entity2)
    }

    "should return all objects in area with negative positions" {
        val world = World(events)
        val entity = world.entities.create("entity", 0f, 0f, 0f)
        val entity2 = world.entities.create("entity2", 0f, -1f, 0f)
        world.entities.create("entity3", -0.5f, -0.5f, 0f)

        val area = Box(
            pos = Vector3(0.5f, -0.5f, 0f),
            size = Vector3(0.5f, 0.5f, 0.5f)
        )

        world.entities.findAllIn(area).shouldContainAll(entity, entity2)
    }
})
