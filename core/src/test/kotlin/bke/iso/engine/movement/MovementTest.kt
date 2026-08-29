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

class MovementTest : StringSpec({

    val collisionBoxes = mockk<CollisionBoxes>()
    every { collisionBoxes[any()] } returns null
    every { collisionBoxes.invalidate(any()) } returns Unit

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
