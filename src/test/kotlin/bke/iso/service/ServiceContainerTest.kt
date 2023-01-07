package bke.iso.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ServiceContainerTest {
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
            ServiceContainer(setOf(B::class, C::class, A::class))
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
    fun whenGetTransientProvider_thenReturnUniqueInstance() {
        @Transient
        class Service

        val container = ServiceContainer(setOf(Service::class))
        val provider = container.getProvider<Service>()
        val service = provider.get()
        val service2 = provider.get()
        assertThat(service).isNotSameAs(service2)
    }

    @Test
    fun whenGetSingletonProvider_thenReturnSameInstance() {
        @Singleton
        class Service

        val container = ServiceContainer(setOf(Service::class))
        val provider = container.getProvider<Service>()
        val service = provider.get()
        val service2 = provider.get()
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
}
