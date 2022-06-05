package bke.iso.di

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ServiceContainerTest {

    @Test
    fun `should register service`() {
        class A {
            fun getText() = "text"
        }

        val container = ServiceContainer()
        container.registerService(A::class)

        val service = container.getService<A>()
        assertThat(service.getText()).isEqualTo("text")
    }

    @Test
    fun `should return instance of implementation type given an interface type`() {
        abstract class Animal {
            abstract fun getType(): String
        }

        class Cat : Animal() {
            override fun getType() = "cat"
        }

        val container = ServiceContainer()
        container.registerService(Animal::class, Cat::class)

        val service = container.getService<Animal>()
        assertThat(service.getType()).isEqualTo("cat")
    }
}
