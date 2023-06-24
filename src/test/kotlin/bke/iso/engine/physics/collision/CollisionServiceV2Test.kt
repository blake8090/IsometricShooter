package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.world.WorldService
import com.badlogic.gdx.math.Vector3
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

class CollisionServiceV2Test {

    @Test
    fun `when findCollisionData, given missing component, then return null`() {
        val entity = Entity(UUID.randomUUID())

        val collisionService = CollisionServiceV2(mock(WorldService::class.java))
        assertThat(collisionService.findCollisionData(entity)).isNull()
    }

    @Test
    fun `when findCollisionData, then return collision data`() {
        val dimensions = Vector3(1f, 1f, 1f)
        val entity = createEntity(dimensions)
        entity.x = 2f
        entity.y = 2f
        entity.z = 2f

        val collisionService = CollisionServiceV2(mock(WorldService::class.java))
        val data = collisionService.findCollisionData(entity) ?: fail("expected collision data")

        assertThat(data.bounds.dimensions).isEqualTo(dimensions)
        assertThat(data.box).isEqualTo(
            Box(
                Vector3(2f, 2f, 2f),
                1f,
                1f,
                1f
            )
        )
    }

    /**
     *
     *    +--------+
     *   /        /|
     *  /        / |
     * +--------+  |
     * |        |  |
     * |        |  +
     * |        | /|
     * |        |/ |
     * +--------+  +
     * |        | /
     * |        |/
     * +--------+
     *
     *
     */
    @Test
    fun `when predictEntityCollisions, given entity is moving left, then return collisions`() {
        val entity = createEntity(Vector3(0.5f, 0.5f, 0.5f))
        entity.x = 1f

        val entity2 = createEntity(Vector3(1f, 1f, 1f))

        val worldService = mock(WorldService::class.java)
        `when`(worldService.getObjectsAt(0, 0, 0)).thenReturn(setOf(entity2))

        val collisionService = CollisionServiceV2(worldService)

        val predictedCollisions = collisionService.predictEntityCollisions(entity, -0.2f, 0f, 0f)
        assertThat(predictedCollisions).isNotNull
        // TODO: finish assertion
    }

    private fun createEntity(dimensions: Vector3, offset: Vector3 = Vector3()) =
        Entity(UUID.randomUUID())
            .add(
                Collider(
                    Bounds(dimensions, offset), true
                )
            )
}
