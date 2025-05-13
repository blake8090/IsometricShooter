package bke.iso.engine.world.entity

import bke.iso.engine.math.Location
import bke.iso.engine.collision.Collider
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class EntityTest : StringSpec({

    "should return locations" {
        val entity = Entity("")
        entity.add(Collider(Vector3(1f, 1f, 1f)))

        val locations = entity.getLocations()
        locations.shouldContainExactlyInAnyOrder(
            Location(0, 0, 0),
            Location(0, 1, 0),
            Location(1, 0, 0),
            Location(1, 1, 0),
            Location(0, 0, 1),
            Location(0, 1, 1),
            Location(1, 0, 1),
            Location(1, 1, 1)
        )
    }

    "should return location with negative coordinates" {
        val entity = Entity("")
        entity.moveTo(-1.5f, -2.01f, -0.5f)

        val locations = entity.getLocations()
        locations.shouldContainExactlyInAnyOrder(
            Location(-2, -3, -1)
        )
    }

    "should return locations with both positive and negative coordinates" {
        val entity = Entity("")
        entity.moveTo(1f,  0f, 0f)
        entity.add(
            Collider(
                Vector3(1f, 1f, 1f),
                // center box on entity's position
                Vector3(-0.5f, -0.5f, -0.5f)
            )
        )

        val locations = entity.getLocations()
        locations.shouldContainExactlyInAnyOrder(
            Location(1, 0, 0),
            Location(1, -1, 0),
            Location(0, 0, 0),
            Location(0, -1, 0),
            Location(1, 0, -1),
            Location(1, -1, -1),
            Location(0, 0, -1),
            Location(0, -1, -1),
        )
    }

    "should clamp z for small negative values" {
        val z = 0.00484398f
        val dz = -0.00484401f
        val entity = Entity("")
        entity.moveTo(0f, 0f, z)

        entity.move(0f, 0f, dz)
        entity.z.shouldBe(0f)
    }

    "should clamp z for small positive values" {
        val z = 0f
        val dz = 0.000000125f
        val entity = Entity("")
        entity.moveTo(0f, 0f, z)

        entity.move(0f, 0f, dz)
        entity.z.shouldBe(0f)
    }
})
