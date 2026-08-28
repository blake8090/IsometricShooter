package bke.iso.engine.lighting

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Events
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class LightingTest : StringSpec({
    fun createWorld(): World =
        World(Events {}, CollisionBoxes())

    "point lights should serialize radius color and offset" {
        val serializer = Serializer()
        val components: List<Component> = listOf(
            PointLight(
                intensity = 0.75f,
                radius = 6.5f,
                color = Color(0.25f, 0.5f, 0.75f, 1f),
                offset = Vector3(1f, 2f, 3f)
            )
        )

        val json = serializer.write(components)
        val decoded = serializer.read<List<Component>>(json).single() as PointLight

        decoded.intensity shouldBe 0.75f
        decoded.radius shouldBe 6.5f
        decoded.color shouldBe Color(0.25f, 0.5f, 0.75f, 1f)
        decoded.offset shouldBe Vector3(1f, 2f, 3f)
        json.contains("falloff") shouldBe false
    }

    "lighting should discover point lights and expose live values" {
        val world = createWorld()
        val lighting = Lighting(world)
        val pointLight = PointLight(intensity = 0.5f)
        val lightEntity = world.entities.create(
            id = "light",
            x = 1f,
            y = 2f,
            z = 3f,
            pointLight
        )
        world.entities.create(
            id = "not-a-light",
            x = 0f,
            y = 0f,
            z = 0f
        )

        lighting.update(0f)
        val ids = mutableListOf<String>()
        lighting.forEachPointLight { entity, light ->
            ids.add(entity.id)
            light.intensity shouldBe 0.5f
        }
        ids.shouldContainExactly("light")

        pointLight.intensity = 0.8f
        lightEntity.moveTo(4f, 5f, 6f)
        lighting.forEachPointLight { entity, light ->
            entity.pos shouldBe Vector3(4f, 5f, 6f)
            light.intensity shouldBe 0.8f
        }
    }

    "lighting should stop exposing removed or cleared lights" {
        val world = createWorld()
        val lighting = Lighting(world)
        val entity = world.entities.create(
            id = "light",
            x = 0f,
            y = 0f,
            z = 0f,
            PointLight()
        )

        lighting.update(0f)
        entity.remove<PointLight>()
        var count = 0
        lighting.forEachPointLight { _, _ -> count++ }
        count shouldBe 0

        entity.add(PointLight())
        lighting.update(0f)
        lighting.forEachPointLight { _, _ -> count++ }
        count shouldBe 1

        lighting.clear()
        count = 0
        lighting.forEachPointLight { _, _ -> count++ }
        count shouldBe 0

        lighting.update(0f)
        world.clear()
        lighting.update(0f)
        lighting.forEachPointLight { _, _ -> count++ }
        count shouldBe 0
    }
})
