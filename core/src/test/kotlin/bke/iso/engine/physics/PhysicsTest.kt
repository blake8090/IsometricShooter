package bke.iso.engine.physics

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.PredictedCollision
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import bke.iso.engine.world.World
import bke.iso.engine.world.event.EntityComponentAdded
import bke.iso.engine.world.event.EntityComponentRemoved
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class PhysicsTest : StringSpec({

    "should not create ground contact from an unresolved prediction" {
        val collisionBoxes = mockk<CollisionBoxes>()
        every { collisionBoxes[any()] } returns null
        every { collisionBoxes.invalidate(any()) } returns Unit

        val world = World(Events {}, collisionBoxes)
        val entity = world.entities.create(
            "body",
            0f,
            0f,
            1f,
            PhysicsBody(mode = PhysicsMode.DYNAMIC)
        )
        val ghost = world.entities.create(
            "ghost",
            0f,
            0f,
            0f,
            PhysicsBody(mode = PhysicsMode.GHOST)
        )
        val ghostBox = Box.fromMinMax(Vector3(-1f, -1f, 0f), Vector3(1f, 1f, 1f))
        val predictedGroundCollision = PredictedCollision(
            entity = ghost,
            box = ghostBox,
            distance = 0f,
            collisionTime = 0f,
            hitNormal = Vector3(0f, 0f, 1f),
            side = CollisionSide.TOP
        )
        val collisions = mockk<Collisions>()
        every { collisions.predictCollisions(entity, any()) } returns setOf(predictedGroundCollision)
        every { collisions.predictCollisions(ghost, any()) } returns emptySet()

        Physics(world, collisions, collisionBoxes).update(1f / 60f)

        entity.has<GroundContact>() shouldBe false
    }

    "should retain a resolved ground contact without replacing it every frame" {
        val firedEvents = mutableListOf<Event>()
        val collisionBoxes = mockk<CollisionBoxes>()
        every { collisionBoxes[any()] } returns null
        every { collisionBoxes.invalidate(any()) } returns Unit

        val world = World(Events(firedEvents::add), collisionBoxes)
        val body = PhysicsBody(mode = PhysicsMode.DYNAMIC)
        val entity = world.entities.create("body", 0f, 0f, 1f, body)
        val support = world.entities.create("support", 0f, 0f, 0f)
        val bodyBox = Box.fromMinMax(Vector3(-0.5f, -0.5f, 1f), Vector3(0.5f, 0.5f, 2f))
        val supportBox = Box.fromMinMax(Vector3(-1f, -1f, 0f), Vector3(1f, 1f, 1f))
        every { collisionBoxes[entity] } returns bodyBox
        every { collisionBoxes[support] } returns supportBox

        val groundCollision = PredictedCollision(
            entity = support,
            box = supportBox,
            distance = 0f,
            collisionTime = 0f,
            hitNormal = Vector3(0f, 0f, 1f),
            side = CollisionSide.TOP
        )
        val collisions = mockk<Collisions>()
        every { collisions.predictCollisions(entity, any()) } returnsMany listOf(
            setOf(groundCollision),
            emptySet(),
            setOf(groundCollision),
            emptySet(),
            emptySet()
        )

        val physics = Physics(world, collisions, collisionBoxes)
        firedEvents.clear()
        physics.update(1f / 60f)

        val initialContact = entity.get<GroundContact>()
        initialContact?.support shouldBe support
        firedEvents.groundContactAdditions() shouldBe 1

        firedEvents.clear()
        physics.update(1f / 60f)

        (entity.get<GroundContact>() === initialContact) shouldBe true
        firedEvents.groundContactAdditions() shouldBe 0

        firedEvents.clear()
        physics.update(1f / 60f)

        entity.has<GroundContact>() shouldBe false
        firedEvents.groundContactRemovals() shouldBe 1
    }
})

private fun List<Event>.groundContactAdditions(): Int =
    filterIsInstance<EntityComponentAdded>().count { it.component is GroundContact }

private fun List<Event>.groundContactRemovals(): Int =
    filterIsInstance<EntityComponentRemoved>().count { it.component is GroundContact }
