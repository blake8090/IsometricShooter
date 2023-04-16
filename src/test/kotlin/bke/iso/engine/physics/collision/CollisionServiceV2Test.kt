package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import com.badlogic.gdx.math.Vector3
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.util.UUID

class CollisionServiceV2Test {

    @Test
    fun `when findCollisionData, given missing component, then return null`() {
        val entity = Entity(UUID.randomUUID())

        val collisionService = CollisionServiceV2()
        assertThat(collisionService.findCollisionData(entity)).isNull()
    }

    @Test
    fun `when findCollisionData, then return collision data`() {
        val entity = Entity(UUID.randomUUID())
        entity.x = 2f
        entity.y = 2f
        entity.z = 2f

        val bounds = BoundsV2(Vector3(1f, 1f, 1f))
        entity.add(
            CollisionV2(
                bounds,
                false
            )
        )

        val collisionService = CollisionServiceV2()
        val data = collisionService.findCollisionData(entity) ?: fail("expected collision data")
        assertThat(data.bounds).isEqualTo(bounds)
        assertThat(data.box).isEqualTo(
            Box(
                Vector3(2f, 2f, 2f),
                1f,
                1f,
                1f
            )
        )
    }
}
