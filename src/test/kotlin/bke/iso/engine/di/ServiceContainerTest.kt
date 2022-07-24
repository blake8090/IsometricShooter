package bke.iso.engine.di

import bke.iso.engine.di.test.ExampleSingletonService
import bke.iso.engine.di.test.Rectangle
import bke.iso.engine.di.test.Shape
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/*
TODO: resolve the following issues:
 - test negative states (duplicate implementation, missing binding, duplicate binding)
 - test createInstance
 - dependency handling is NOT currently tested!
 - test exception thrown when implementation class not instance of interface class
 */
internal class ServiceContainerTest {
    @Test
    fun `should register services in classpath`() {
        val container = ServiceContainer("bke.iso.engine.di.test")

        // should not throw an exception
        container.getService<ExampleSingletonService>()
        val shape = container.getService<Shape>()
        assertThat(shape).isInstanceOf(Rectangle::class.java)
    }

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
        abstract class Animal
        class Cat : Animal()

        val container = ServiceContainer()
        container.registerService(Animal::class, Cat::class)

        val service = container.getService<Animal>()
        assertThat(service).isInstanceOf(Cat::class.java)
    }
}
