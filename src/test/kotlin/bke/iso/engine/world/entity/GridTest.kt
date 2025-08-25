package bke.iso.engine.world.entity

import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Location
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class GridTest : StringSpec({

    "should return all entities" {
        val entity = Entity("1")
        val entity2 = Entity("2")

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)

        grid.entities
            .toList()
            .shouldContainExactlyInAnyOrder(entity, entity2)
    }

    "should return entities at location" {
        val entity = Entity("1")
        val entity2 = Entity("2")
        val entity3 = Entity("3")

        entity.moveTo(1f, 1f, 0f)
        entity2.moveTo(1f, 1f, 0f)
        entity3.moveTo(2f, 1f, 0f)

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)
        grid.update(entity3)

        grid[Location(1, 1, 0)]
            .toList()
            .shouldContainExactlyInAnyOrder(entity, entity2)
    }

    "should update entity locations" {
        val entity = Entity("1")
        val entity2 = Entity("2")

        entity.moveTo(1f, 1f, 0f)
        entity2.moveTo(1f, 1f, 0f)

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)

        entity.moveTo(2f, 1f, 0f)
        grid.update(entity)

        grid[Location(1, 1, 0)]
            .toList()
            .shouldContainExactlyInAnyOrder(entity2)
        grid[Location(2, 1, 0)]
            .toList()
            .shouldContainExactlyInAnyOrder(entity)
    }

    "should return entity spanning multiple locations" {
        val entity = Entity("1")
        entity.add(Collider(size = Vector3(2f, 2f, 1f)))

        val grid = Grid()
        grid.update(entity)

        grid[Location(0, 0, 0)].shouldContainExactly(entity)
        grid[Location(0, 1, 0)].shouldContainExactly(entity)
        grid[Location(1, 0, 0)].shouldContainExactly(entity)
        grid[Location(1, 1, 0)].shouldContainExactly(entity)
    }

    "should delete entity" {
        val entity = Entity("1")
        val entity2 = Entity("2")

        entity.moveTo(1f, 1f, 0f)
        entity2.moveTo(1f, 1f, 0f)

        val grid = Grid()
        grid.update(entity)
        grid.update(entity2)
        grid.delete(entity)

        grid[Location(1, 1, 0)].shouldContainExactly(entity2)
        grid.entities.shouldContainExactly(entity2)
    }

    "should return false if no locations changed" {
        val entity = Entity("1")

        entity.moveTo(1f, 1f, 1f)

        val grid = Grid()
        grid.update(entity) shouldBe true

        entity.moveTo(1f, 1f, 1f)
        grid.update(entity) shouldBe false
    }

    "should return true if locations changed" {
        val entity = Entity("1")

        entity.moveTo(1f, 1f, 1f)

        val grid = Grid()
        grid.update(entity) shouldBe true

        entity.moveTo(1f, 1f, 2f)
        grid.update(entity) shouldBe true
    }
})
