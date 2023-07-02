//package bke.iso.engine.physics.collision
//
//import bke.iso.engine.entity.Entity
//import bke.iso.engine.math.Box
//import bke.iso.engine.world.WorldService
//import com.badlogic.gdx.math.Vector3
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.fail
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.`when`
//import java.util.UUID
//
//class CollisionServiceV2Test {
//
//    @Test
//    fun `when findCollisionData, given missing component, then return null`() {
//        val entity = Entity(UUID.randomUUID())
//
//        val collisionService = CollisionServiceV2(mock(WorldService::class.java))
//        assertThat(collisionService.findCollisionData(entity)).isNull()
//    }
//
//    @Test
//    fun `when findCollisionData, then return collision data`() {
//        val dimensions = Vector3(1f, 1f, 1f)
//        val entity = createEntity(dimensions)
//        entity.x = 2f
//        entity.y = 2f
//        entity.z = 2f
//
//        val collisionService = CollisionServiceV2(mock(WorldService::class.java))
//        val data = collisionService.findCollisionData(entity) ?: fail("expected collision data")
//
//        assertThat(data.bounds.dimensions).isEqualTo(dimensions)
//        assertThat(data.box).isEqualTo(
//            Box(
//                Vector3(2f, 2f, 2f),
//                1f,
//                1f,
//                1f
//            )
//        )
//    }
//
//    @Test
//    fun `when predictEntityCollisions, given entity is moving left, then return collisions`() {
//        val entity = createEntity(Vector3(1f, 1f, 1f))
//        entity.x = 1f
//
//        val entity2 = createEntity(Vector3(1f, 1f, 1f))
//
//        val worldService = mock(WorldService::class.java)
//        `when`(worldService.getObjectsAt(0, 0, 0)).thenReturn(setOf(entity2))
//
//        val collisionService = CollisionServiceV2(worldService)
//
//        val predictedCollisions = collisionService.predictEntityCollisions(entity, -0.1f, 0f, 0f)
//        assertThat(predictedCollisions).isNotNull
//
//        assertThat(predictedCollisions!!.collisions).containsExactly(
//            EntityBoxCollision(
//                entity2,
//                EntityCollisionData(
//                    Bounds(Vector3(1f, 1f, 1f)),
//                    Box(Vector3(), 1f, 1f, 1f),
//                    true
//                ),
//                BoxCollisionSide.RIGHT
//            )
//        )
//    }
//
//    @Test
//    fun `when predictEntityCollisions, given entity is moving right, then return collisions`() {
//        val entity = createEntity(Vector3(1f, 1f, 1f))
//
//        val entity2 = createEntity(Vector3(1f, 1f, 1f))
//        entity2.x = 1f
//
//        val worldService = mock(WorldService::class.java)
//        `when`(worldService.getObjectsAt(1, 0, 0)).thenReturn(setOf(entity2))
//
//        val collisionService = CollisionServiceV2(worldService)
//
//        val predictedCollisions = collisionService.predictEntityCollisions(entity, 0.1f, 0f, 0f)
//        assertThat(predictedCollisions).isNotNull
//
//        assertThat(predictedCollisions!!.collisions).containsExactly(
//            EntityBoxCollision(
//                entity2,
//                EntityCollisionData(
//                    Bounds(Vector3(1f, 1f, 1f)),
//                    Box(Vector3(1f, 0f, 0f), 1f, 1f, 1f),
//                    true
//                ),
//                BoxCollisionSide.LEFT
//            )
//        )
//    }
//
//    @Test
//    fun `when predictEntityCollisions, given entity is moving forwards, then return collisions`() {
//        val entity = createEntity(Vector3(1f, 1f, 1f))
//
//        val entity2 = createEntity(Vector3(1f, 1f, 1f))
//        entity2.y = 1f
//
//        val worldService = mock(WorldService::class.java)
//        `when`(worldService.getObjectsAt(0, 1, 0)).thenReturn(setOf(entity2))
//
//        val collisionService = CollisionServiceV2(worldService)
//
//        val predictedCollisions = collisionService.predictEntityCollisions(entity, 0f, 0.1f, 0f)
//        assertThat(predictedCollisions).isNotNull
//
//        assertThat(predictedCollisions!!.collisions).containsExactly(
//            EntityBoxCollision(
//                entity2,
//                EntityCollisionData(
//                    Bounds(Vector3(1f, 1f, 1f)),
//                    Box(Vector3(0f, 1f, 0f), 1f, 1f, 1f),
//                    true
//                ),
//                BoxCollisionSide.FRONT
//            )
//        )
//    }
//
//    @Test
//    fun `when predictEntityCollisions, given entity is moving backwards, then return collisions`() {
//        val entity = createEntity(Vector3(1f, 1f, 1f))
//        entity.y = 1f
//
//        val entity2 = createEntity(Vector3(1f, 1f, 1f))
//
//        val worldService = mock(WorldService::class.java)
//        `when`(worldService.getObjectsAt(0, 0, 0)).thenReturn(setOf(entity2))
//
//        val collisionService = CollisionServiceV2(worldService)
//
//        val predictedCollisions = collisionService.predictEntityCollisions(entity, 0f, -0.1f, 0f)
//        assertThat(predictedCollisions).isNotNull
//
//        assertThat(predictedCollisions!!.collisions).containsExactly(
//            EntityBoxCollision(
//                entity2,
//                EntityCollisionData(
//                    Bounds(Vector3(1f, 1f, 1f)),
//                    Box(Vector3(0f, 0f, 0f), 1f, 1f, 1f),
//                    true
//                ),
//                BoxCollisionSide.BACK
//            )
//        )
//    }
//
//    @Test
//    fun `when predictEntityCollisions, given entity is moving down, then return collisions`() {
//        val entity = createEntity(Vector3(1f, 1f, 1f))
//        entity.z = 1f
//
//        val entity2 = createEntity(Vector3(1f, 1f, 1f))
//
//        val worldService = mock(WorldService::class.java)
//        `when`(worldService.getObjectsAt(0, 0, 0)).thenReturn(setOf(entity2))
//
//        val collisionService = CollisionServiceV2(worldService)
//
//        val predictedCollisions = collisionService.predictEntityCollisions(entity, 0f, 0f, -0.1f)
//        assertThat(predictedCollisions).isNotNull
//
//        assertThat(predictedCollisions!!.collisions).containsExactly(
//            EntityBoxCollision(
//                entity2,
//                EntityCollisionData(
//                    Bounds(Vector3(1f, 1f, 1f)),
//                    Box(Vector3(0f, 0f, 0f), 1f, 1f, 1f),
//                    true
//                ),
//                BoxCollisionSide.TOP
//            )
//        )
//    }
//
//    @Test
//    fun `when predictEntityCollisions, given entity is moving up, then return collisions`() {
//        val entity = createEntity(Vector3(1f, 1f, 1f))
//
//        val entity2 = createEntity(Vector3(1f, 1f, 1f))
//        entity2.z = 1f
//
//        val worldService = mock(WorldService::class.java)
//        `when`(worldService.getObjectsAt(0, 0, 1)).thenReturn(setOf(entity2))
//
//        val collisionService = CollisionServiceV2(worldService)
//
//        val predictedCollisions = collisionService.predictEntityCollisions(entity, 0f, 0f, 0.1f)
//        assertThat(predictedCollisions).isNotNull
//
//        assertThat(predictedCollisions!!.collisions).containsExactly(
//            EntityBoxCollision(
//                entity2,
//                EntityCollisionData(
//                    Bounds(Vector3(1f, 1f, 1f)),
//                    Box(Vector3(0f, 0f, 1f), 1f, 1f, 1f),
//                    true
//                ),
//                BoxCollisionSide.BOTTOM
//            )
//        )
//    }
//
//    private fun createEntity(dimensions: Vector3, offset: Vector3 = Vector3()) =
//        Entity(UUID.randomUUID())
//            .add(
//                Collider(
//                    Bounds(dimensions, offset), true
//                )
//            )
//}
