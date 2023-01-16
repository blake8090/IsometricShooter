package bke.iso.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ServiceContainerTest {

    @Test
    @Suppress("UNUSED_PARAMETER")
    fun whenGivenServicesInAnyOrder_thenExpectNoError() {
        @Singleton
        class A

        @Singleton
        class B(a: A)

        @Singleton
        class C(b: B)

        assertDoesNotThrow {
            ServiceContainer(setOf(A::class, C::class, B::class))
        }
    }

    @Test
    fun whenGetTransientService_thenReturnUniqueInstance() {
        @Transient
        class Service

        val container = ServiceContainer(setOf(Service::class))
        val service = container.get<Service>()
        val service2 = container.get<Service>()
        assertThat(service).isNotSameAs(service2)
    }

    @Test
    fun whenGetSingletonService_thenReturnSameInstance() {
        @Singleton
        class Service

        val container = ServiceContainer(setOf(Service::class))
        val service = container.get<Service>()
        val service2 = container.get<Service>()
        assertThat(service).isSameAs(service2)
    }

    @Test
    fun givenServices_thenInjectSingletonDependency() {
        @Singleton
        class SingletonService

        @Transient
        class TransientService(val singletonService: SingletonService)

        val container = ServiceContainer(setOf(SingletonService::class, TransientService::class))
        val service = container.get<TransientService>()
        val service2 = container.get<TransientService>()
        assertThat(service.singletonService).isSameAs(service2.singletonService)
    }

    @Test
    fun givenServices_thenInjectTransientDependency() {
        @Transient
        class TransientService

        @Singleton
        class SingletonService(val transientService: TransientService)

        @Singleton
        class AnotherSingletonService(val transientService: TransientService)

        val container = ServiceContainer(
            setOf(
                SingletonService::class,
                AnotherSingletonService::class,
                TransientService::class
            )
        )

        val service = container.get<SingletonService>()
        val service2 = container.get<AnotherSingletonService>()
        assertThat(service.transientService).isNotSameAs(service2.transientService)
    }

    @Test
    fun givenServices_thenInjectProviderDependency() {
        @Transient
        class A {
            val value = 5
        }

        @Singleton
        class B(private val provider: Provider<A>) {
            fun getValue() =
                provider.get().value
        }

        val container = ServiceContainer(setOf(B::class, A::class))

        val service = container.get<B>()
        assertThat(service.getValue()).isEqualTo(5)
    }

    @Nested
    @DisplayName("Error Cases")
    inner class ErrorCases {
        @Nested
        @DisplayName("Circular Dependency")
        @Suppress("UNUSED_PARAMETER")
        inner class CircularDependencyCase {
            @Singleton
            inner class A(b: B)

            @Singleton
            inner class B(c: C)

            @Singleton
            inner class C(a: A)

            @Test
            fun `Given list of classes with circular dependency, When create container, Then throw exception`() {
                val error = assertThrows<CircularDependencyException> {
                    ServiceContainer(setOf(A::class, B::class, C::class))
                }
                assertThat(error.message).isEqualTo("Found circular dependency: A -> B -> C -> A")
            }
        }

        @Nested
        @DisplayName("Nested Circular Dependency")
        @Suppress("UNUSED_PARAMETER")
        inner class NestedCircularDependencyCase {
            @Singleton
            inner class A(b: B)

            @Singleton
            inner class B(c: C, d: D)

            @Singleton
            inner class C

            @Singleton
            inner class D(a: A)

            @Test
            fun `Given list of classes with nested circular dependency, When create container, Then throw exception`() {
                val error = assertThrows<CircularDependencyException> {
                    ServiceContainer(setOf(A::class, B::class, C::class, D::class))
                }
                assertThat(error.message).isEqualTo("Found circular dependency: A -> B -> D -> A")
            }
        }

        /**
         * A -> B, C
         * B -> nothing
         * C -> D -> E -> C
         *
         * A -> B, C -> D -> E -> C
         */
        @Nested
        @DisplayName("Nested Circular Dependency")
        @Suppress("UNUSED_PARAMETER")
        inner class NestedCircularDependencyCase2 {
            @Singleton
            inner class A(b: B, c: C)

            @Singleton
            inner class B

            @Singleton
            inner class C(d: D)

            @Singleton
            inner class D(e: E)

            @Singleton
            inner class E(c: C)

            @Test
            fun `Given list of classes with nested circular dependency, When create container, Then throw exception`() {
                val error = assertThrows<CircularDependencyException> {
                    ServiceContainer(setOf(A::class, B::class, C::class, D::class, E::class))
                }
                assertThat(error.message).isEqualTo("Found circular dependency: A -> C -> D -> E -> C")
            }
        }

        @Test
        fun `Given no annotations on class, When create container, Then throw exception`() {
            class A

            assertThrows<MissingAnnotationsException> {
                ServiceContainer(setOf(A::class))
            }
        }
    }
}
