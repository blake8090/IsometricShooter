package bke.iso.world.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EntityDatabaseTest {
    @Test
    fun `should create and return new entity id`() {
        val entityDatabase = EntityDatabase()

        assertThat(entityDatabase.createEntity()).isEqualTo(1)
        assertThat(entityDatabase.createEntity()).isEqualTo(2)
        assertThat(entityDatabase.createEntity()).isEqualTo(3)
    }

    @Test
    fun `should return true if entity id exists, false otherwise`() {
        val entityDatabase = EntityDatabase()

        entityDatabase.createEntity()
        assertThat(entityDatabase.contains(1)).isTrue
        assertThat(entityDatabase.contains(2)).isFalse
    }

    @Test
    fun `given an entity id, should both set and find components`() {
        class ExampleComponent : Component()
        class AnotherExampleComponent : Component()

        val entityDatabase = EntityDatabase()

        val id = entityDatabase.createEntity()
        entityDatabase.setComponent(id, ExampleComponent())
        assertThat(entityDatabase.findComponent<ExampleComponent>(id)).isNotNull
        assertThat(entityDatabase.findComponent<AnotherExampleComponent>(id)).isNull()
    }

    @Test
    fun `should return entities with a specific component type`() {
        class A : Component()
        class B : Component()
        class C : Component()

        val entityDatabase = EntityDatabase()

        val entity1 = entityDatabase.createEntity()
        entityDatabase.setComponent(entity1, A())

        val entity2 = entityDatabase.createEntity()
        entityDatabase.setComponent(entity2, B())

        val entity3 = entityDatabase.createEntity()
        entityDatabase.setComponent(entity3, A())
        entityDatabase.setComponent(entity3, B())

        assertThat(entityDatabase.findEntitiesWithComponent<A>())
            .containsExactlyInAnyOrder(entity1, entity3)

        assertThat(entityDatabase.findEntitiesWithComponent<B>())
            .containsExactlyInAnyOrder(entity2, entity3)

        assertThat(entityDatabase.findEntitiesWithComponent<C>()).isEmpty()
    }
}