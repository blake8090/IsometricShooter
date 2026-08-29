package bke.iso.engine.movement

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.core.Events
import bke.iso.engine.physics.GroundContact
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.math.abs

class MovementTest : StringSpec({

    val collisionBoxes = mockk<CollisionBoxes>()
    every { collisionBoxes[any()] } returns null
    every { collisionBoxes.invalidate(any()) } returns Unit

    "should preserve analog movement magnitude" {
        val world = World(Events {}, collisionBoxes)
        val body = PhysicsBody(mode = PhysicsMode.DYNAMIC)
        world.entities.create(
            "mover",
            0f,
            0f,
            0f,
            body,
            MovementProperties(maxSpeed = 10f),
            MovementIntent(Vector3(0.2f, 0f, 0f))
        )

        Movement(world).update(1f / 60f)

        body.velocity shouldBe Vector3(2f, 0f, 0f)
    }

    "should clamp planar movement magnitude to maximum speed" {
        val world = World(Events {}, collisionBoxes)
        val body = PhysicsBody(mode = PhysicsMode.DYNAMIC)
        world.entities.create(
            "mover",
            0f,
            0f,
            0f,
            body,
            MovementProperties(maxSpeed = 10f),
            MovementIntent(Vector3(1f, 1f, 0f))
        )

        Movement(world).update(1f / 60f)

        (abs(body.velocity.len() - 10f) < 0.0001f) shouldBe true
    }

    "should discard a jump intent while airborne" {
        val world = World(Events {}, collisionBoxes)
        val body = PhysicsBody(mode = PhysicsMode.DYNAMIC)
        val entity = world.entities.create(
            "jumper",
            0f,
            0f,
            0f,
            body,
            JumpProperties(speed = 5f),
            JumpIntent()
        )

        Movement(world).update(1f / 60f)

        entity.has<JumpIntent>() shouldBe false
        body.pendingImpulse shouldBe Vector3.Zero
    }

    "should apply a jump velocity change while grounded" {
        val world = World(Events {}, collisionBoxes)
        val support = world.entities.create("support", 0f, 0f, 0f)
        val body = PhysicsBody(mode = PhysicsMode.DYNAMIC, mass = 2f)
        val entity = world.entities.create(
            "jumper",
            0f,
            0f,
            0f,
            body,
            JumpProperties(speed = 5f),
            JumpIntent(),
            GroundContact(support)
        )

        Movement(world).update(1f / 60f)

        entity.has<JumpIntent>() shouldBe false
        entity.has<GroundContact>() shouldBe false
        body.pendingImpulse shouldBe Vector3(0f, 0f, 10f)
    }
})
