package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.physics.Collider
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize

class ActorTest : StringSpec({

    "should return locations" {
        val actor = Actor()
        actor.add(Collider(false, Vector3(1f, 1f, 1f)))

        val locations = actor.getLocations()
        locations.shouldHaveSize(8)
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
})
