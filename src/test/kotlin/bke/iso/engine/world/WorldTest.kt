package bke.iso.engine.world

import bke.iso.engine.Game
import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.mockk

class WorldTest : StringSpec({

    "should return all objects in area" {
        val world = World(mockk<Game>())
        val actor = world.newActor(0f, 0f, 0f)
        val actor2 = world.newActor(0f, 0f, 1.5f)
        val actor3 = world.newActor(1f, 1f, 1f)
        world.newActor(1f, 1f, 2.1f)

        val area = Box(Vector3(0.5f, 0.5f, 0.5f), Vector3(1f, 1f, 1f))
        world.getObjectsInArea(area).shouldContainExactlyInAnyOrder(actor, actor2, actor3)
    }
})
