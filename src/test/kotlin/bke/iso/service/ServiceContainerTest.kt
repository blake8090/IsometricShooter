package bke.iso.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ServiceContainerTest {
    /*
    test cases:
    ===

    lifetime

    injection
    - singleton
    - transient
    - provider
    - provider (sub type)
*/
    @Singleton
    class CircularDependencyA(b: CircularDependencyB)

    @Singleton
    class CircularDependencyB(c: CircularDependencyC)

    @Singleton
    class CircularDependencyC(a: CircularDependencyA)

    @Test
    fun whenCircularDependencyFound_thenError() {
        val error = assertThrows<Error> {
            ServiceContainer(
                setOf(
                    CircularDependencyA::class,
                    CircularDependencyB::class,
                    CircularDependencyC::class
                )
            )
        }
        assertThat(error.message).isEqualTo(
            "circular dependency: CircularDependencyA, CircularDependencyB, CircularDependencyC -> CircularDependencyA"
        )
    }

    @Test
    fun whenGivenServicesInAnyOrder_thenExpectNoError() {
        @Singleton
        class A

        @Singleton
        class B(a: A)

        @Singleton
        class C(b: B)

        assertDoesNotThrow {
            ServiceContainer(
                setOf(
                    B::class,
                    C::class,
                    A::class
                )
            )
        }
    }

    @Test
    fun whenGetTransientService_thenReturnUniqueInstances() {
        @Transient
        class Service {
            val value = Math.random()
        }

        val container = ServiceContainer(setOf(Service::class))
        val service = container.get<Service>()
        val service2 = container.get<Service>()
        assertThat(service).isNotEqualTo(service2)
        assertThat(service.value).isNotEqualTo(service2.value)
    }

    @Test
    fun whenGetSingletonService_thenReturnSameInstances() {
        @Singleton
        class Service {
            val value = Math.random()
        }

        val container = ServiceContainer(setOf(Service::class))
        val service = container.get<Service>()
        val service2 = container.get<Service>()
        assertThat(service).isEqualTo(service2)
        assertThat(service.value).isEqualTo(service2.value)
    }
}
