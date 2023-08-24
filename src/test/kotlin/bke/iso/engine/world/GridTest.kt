package bke.iso.engine.world

import bke.iso.engine.math.Location
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk

class GridTest : StringSpec({

    "should return actors at location" {
        val actor = mockk<Actor>()
        every { actor.getLocations() } returns setOf(Location(1, 1, 0))
        val actor2 = mockk<Actor>()
        every { actor2.getLocations() } returns setOf(Location(1, 1, 0))

        val grid = Grid()
        grid.update(actor)
        grid.update(actor2)

        grid.getAll(Location(1, 1, 0)).shouldContainExactlyInAnyOrder(actor, actor2)
    }

    "should update actor locations" {
        val actor = mockk<Actor>()
        every { actor.getLocations() } returns setOf(Location(1, 1, 0))
        val actor2 = mockk<Actor>()
        every { actor2.getLocations() } returns setOf(Location(1, 1, 0))

        val grid = Grid()
        grid.update(actor)
        grid.update(actor2)

        every { actor.getLocations() } returns setOf(Location(2, 1, 0))
        grid.update(actor)

        grid.getAll(Location(1, 1, 0)).shouldContainExactlyInAnyOrder(actor2)
        grid.getAll(Location(2, 1, 0)).shouldContainExactlyInAnyOrder(actor)
    }

    "should return actor spanning multiple locations" {
        val actor = mockk<Actor>()
        every { actor.getLocations() } returns setOf(
            Location(0, 0, 0),
            Location(0, 1, 0),
            Location(0, 2, 0)
        )

        val grid = Grid()
        grid.update(actor)

        grid.getAll(Location(0, 0, 0)).shouldContainExactly(actor)
        grid.getAll(Location(0, 1, 0)).shouldContainExactly(actor)
        grid.getAll(Location(0, 2, 0)).shouldContainExactly(actor)
    }

    "should remove actor" {
        val actor = mockk<Actor>()
        every { actor.getLocations() } returns setOf(Location(1, 1, 0))
        val actor2 = mockk<Actor>()
        every { actor2.getLocations() } returns setOf(Location(1, 1, 0))

        val grid = Grid()
        grid.update(actor)
        grid.update(actor2)

        grid.remove(actor)
        grid.getAll(Location(1, 1, 0)).shouldContainExactly(actor2)
        grid.getAllActors().shouldContainExactly(actor2)
    }
})
