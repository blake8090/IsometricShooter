package bke.iso.engine.world.entity

import bke.iso.engine.math.Location
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk

class GridTest : StringSpec({

    "should return objects" {
        val entity = mockk<Entity>()
        every { entity.getLocations() } returns setOf(Location(1, 0, 0))

        val grid = Grid()
        grid.update(entity)

        grid.entities
            .toList()
            .shouldContainExactlyInAnyOrder(entity)
    }

    "should return entities at location" {
        val entity = mockk<Entity>()
        every { entity.getLocations() } returns setOf(Location(1, 1, 0))
        val entity2 = mockk<Entity>()
        every { entity2.getLocations() } returns setOf(Location(1, 1, 0))

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)

        grid[Location(1, 1, 0)]
            .toList()
            .shouldContainExactlyInAnyOrder(entity, entity2)
    }

    "should update entity locations" {
        val entity = mockk<Entity>()
        every { entity.getLocations() } returns setOf(Location(1, 1, 0))
        val entity2 = mockk<Entity>()
        every { entity2.getLocations() } returns setOf(Location(1, 1, 0))

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)

        every { entity.getLocations() } returns setOf(Location(2, 1, 0))
        grid.update(entity)

        grid[Location(1, 1, 0)]
            .toList()
            .shouldContainExactlyInAnyOrder(entity2)
        grid[Location(2, 1, 0)]
            .toList()
            .shouldContainExactlyInAnyOrder(entity)
    }

    "should return entity spanning multiple locations" {
        val entity = mockk<Entity>()
        every { entity.getLocations() } returns setOf(
            Location(0, 0, 0),
            Location(0, 1, 0),
            Location(0, 2, 0)
        )

        val grid = Grid()
        grid.update(entity)

        grid[Location(0, 0, 0)].shouldContainExactly(entity)
        grid[Location(0, 1, 0)].shouldContainExactly(entity)
        grid[Location(0, 2, 0)].shouldContainExactly(entity)
    }

    "should remove entity" {
        val entity = mockk<Entity>()
        every { entity.getLocations() } returns setOf(Location(1, 1, 0))
        val entity2 = mockk<Entity>()
        every { entity2.getLocations() } returns setOf(Location(1, 1, 0))

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)

        grid.delete(entity)
        grid[Location(1, 1, 0)].shouldContainExactly(entity2)
        grid.entities.shouldContainExactly(entity2)
    }
})
