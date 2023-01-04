package bke.iso.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
    @Test
    fun test() {
        @Singleton
        class A

        @Singleton
        class B(val a: A)

        val services = setOf(A::class, B::class)
        val container = ServiceContainer(services)
    }

    @Test
    fun providerTest() {
        @Singleton
        class A {
            val value = 5
        }

        val container = ServiceContainer(setOf(A::class))

        val provider = container.get<Provider<A>>()
        val a = provider.get()
        assertThat(a.value).isEqualTo(5)
    }

    @Test
    fun providerSubTypeTest() {
        abstract class A {
            abstract val value: Int
        }

        @Transient
        class B : A() {
            override val value = 1
        }

        @Transient
        class C : A() {
            override val value = 2
        }

        val container = ServiceContainer(setOf(B::class, C::class))

        val provider = container.get<Provider<A>>()
        assertThat(provider.get(B::class).value).isEqualTo(1)
        assertThat(provider.get(C::class).value).isEqualTo(2)

    }

    @Test
    fun providerInjectionTest() {
        @Transient
        class A {
            val value = 1
        }

        @Transient
        class B(val provider: Provider<A>) {
            fun text(): String {
                val a = provider.get()
                return "value = ${a.value}"
            }
        }

        val container = ServiceContainer(setOf(A::class, B::class))

        val b = container.get<B>()
        assertThat(b.text()).isEqualTo("value = 1")
    }
}
