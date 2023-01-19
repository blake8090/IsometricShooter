package bke.iso.service.cache

import bke.iso.service.CircularDependencyException
import bke.iso.service.PostInit
import bke.iso.service.Provider
import bke.iso.service.Singleton
import bke.iso.service.Transient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ServiceCacheTest {

    @Test
    @Suppress("UNUSED_PARAMETER")
    fun givenServicesInAnyOrder_whenInit_thenNoError() {
        @Singleton
        class A

        @Singleton
        class B(a: A)

        @Singleton
        class C(b: B)

        assertDoesNotThrow {
            val cache = ServiceCache()
            cache.init(setOf(A::class, C::class, B::class))
        }
    }

    @Test
    fun givenTransientService_whenGet_thenReturnUniqueInstance() {
        @Transient
        class Service

        val cache = ServiceCache()
        cache.init(setOf(Service::class))
        val service = cache[Service::class]
        val service2 = cache[Service::class]
        assertThat(service).isNotSameAs(service2)
    }

    @Test
    fun givenSingletonService_whenGet_thenReturnSameInstance() {
        @Singleton
        class Service

        val cache = ServiceCache()
        cache.init(setOf(Service::class))
        val service = cache[Service::class]
        val service2 = cache[Service::class]
        assertThat(service).isSameAs(service2)
    }

    @Test
    fun givenServices_whenInit_thenInjectSingletonDependency() {
        @Singleton
        class SingletonService

        @Transient
        class TransientService(val singletonService: SingletonService)

        @Transient
        class AnotherTransientService(val singletonService: SingletonService)

        val cache = ServiceCache()
        cache.init(setOf(SingletonService::class, TransientService::class, AnotherTransientService::class))
        val service = cache[TransientService::class]
        val service2 = cache[AnotherTransientService::class]
        assertThat(service.singletonService).isSameAs(service2.singletonService)
    }

    @Test
    fun givenServices_whenInit_thenInjectTransientDependency() {
        @Transient
        class TransientService

        @Singleton
        class SingletonService(val transientService: TransientService)

        @Singleton
        class AnotherSingletonService(val transientService: TransientService)

        val cache = ServiceCache()
        cache.init(
            setOf(
                SingletonService::class,
                AnotherSingletonService::class,
                TransientService::class
            )
        )

        val service = cache[SingletonService::class]
        val service2 = cache[AnotherSingletonService::class]
        assertThat(service.transientService).isNotSameAs(service2.transientService)
    }

    @Test
    fun givenServices_whenInit_thenInjectProviderDependency() {
        @Transient
        class A {
            val value = 5
        }

        @Singleton
        class B(private val provider: Provider<A>) {
            fun getValue() =
                provider.get().value
        }

        val cache = ServiceCache()
        cache.init(setOf(B::class, A::class))

        val service = cache[B::class]
        assertThat(service.getValue()).isEqualTo(5)
    }

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
                val cache = ServiceCache()
                cache.init(setOf(A::class, B::class, C::class))
            }
            assertThat(error.message).isEqualTo("Found circular dependency: A -> B -> C -> A")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    class NestedCircularDependencyCase {
        @Singleton
        class A(b: B)

        @Singleton
        class B(c: C, d: D)

        @Singleton
        class C

        @Singleton
        class D(a: A)

        init {
            val cache = ServiceCache()
            cache.init(setOf(A::class, B::class, C::class, D::class))
        }
    }

    @Test
    fun `Given list of classes with nested circular dependency, When create container, Then throw exception`() {
        val error = assertThrows<CircularDependencyException> {
            NestedCircularDependencyCase()
        }
        assertThat(error.message).isEqualTo("Found circular dependency: A -> B -> D -> A")
    }

    @Suppress("UNUSED_PARAMETER")
    class NestedCircularDependencyCase2 {
        @Singleton
        class A(b: B, c: C)

        @Singleton
        class B

        @Singleton
        class C(d: D)

        @Singleton
        class D(e: E)

        @Singleton
        class E(c: C)

        init {
            val cache = ServiceCache()
            cache.init(setOf(A::class, B::class, C::class, D::class, E::class))
        }
    }

    @Test
    fun advancedCircularDependency() {
        val error = assertThrows<CircularDependencyException> {
            NestedCircularDependencyCase2()
        }
        assertThat(error.message).isEqualTo("Found circular dependency: A -> C -> D -> E -> C")
    }

    @Test
    fun whenInitialize_thenCallPostInit() {
        @Singleton
        class Service {
            var value = 5

            @PostInit
            fun setup() {
                value = 2
            }
        }

        val cache = ServiceCache()
        cache.init(setOf(Service::class))

        val service = cache[Service::class]
        assertThat(service.value).isEqualTo(2)
    }

    @Suppress("UNUSED_PARAMETER")
    class CircularProviderDependency {
        @Singleton
        class A(private val provider: Provider<B>) {
            @PostInit
            fun setup() {
                provider.get()
            }
        }

        @Transient
        class B(a: A)

        init {
            val cache = ServiceCache()
            cache.init(setOf(B::class, A::class))
        }
    }

    @Test
    fun givenCircularProviderDependency_whenPostInit_thenDoNotThrowError() {
        assertDoesNotThrow {
            CircularProviderDependency()
        }
    }
}
