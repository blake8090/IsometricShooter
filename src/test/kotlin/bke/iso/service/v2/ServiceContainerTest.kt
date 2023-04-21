package bke.iso.service.v2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ServiceContainerTest {

    @Test
    fun `when register, then register all services`() {
        class A : TransientService
        class B : TransientService
        class C : TransientService

        val container = ServiceContainer()

        assertDoesNotThrow {
            container.register(A::class, B::class, C::class)
            container.get<A>()
            container.get<B>()
            container.get<C>()
        }
    }

    @Test
    @Suppress("UNUSED")
    fun `when register, given nested dependency chain, then register and link all services`() {
        class C : TransientService
        class B(val c: C) : TransientService
        class A(val b: B) : TransientService

        val container = ServiceContainer()

        assertDoesNotThrow {
            container.register(A::class)
            container.get<A>()
            container.get<B>()
            container.get<C>()
        }
    }

    @Test
    fun `when get, given a TransientService, then return a new instance`() {
        class A : TransientService

        val container = ServiceContainer()
        container.register(A::class)

        val a = container.get<A>()
        val a2 = container.get<A>()
        assertThat(a).isNotSameAs(a2)
    }

    @Test
    fun `when get, given a SingletonService, then return the same instance`() {
        class A : SingletonService

        val container = ServiceContainer()
        container.register(A::class)

        val a = container.get<A>()
        val a2 = container.get<A>()
        assertThat(a).isSameAs(a2)
    }

    @Test
    @Suppress("UNUSED")
    fun `when register, given nested SingletonServices, then initialize all of them without exceptions`() {
        class C : SingletonService
        class B(val c: C) : SingletonService
        class A(val b: B) : SingletonService

        val container = ServiceContainer()

        assertDoesNotThrow {
            container.register(A::class, B::class)
            container.get<A>()
            container.get<B>()
        }
    }

    @Test
    @Suppress("UNUSED")
    fun `when register, given singleton dependency, then use same instance`() {
        class C : SingletonService
        class B(val c: C) : TransientService
        class A(val c: C) : TransientService

        val container = ServiceContainer()
        container.register(A::class, B::class, C::class)

        val a = container.get<A>()
        val b = container.get<B>()
        assertThat(a.c).isSameAs(b.c)
    }

    @Test
    @Suppress("UNUSED")
    fun `when register, given transient dependency, then use different instances`() {
        class C : TransientService
        class B(val c: C) : TransientService
        class A(val c: C) : TransientService

        val container = ServiceContainer()
        container.register(A::class, B::class, C::class)

        val a = container.get<A>()
        val b = container.get<B>()
        assertThat(a.c).isNotSameAs(b.c)
    }

    @Test
    @Suppress("UNUSED")
    fun `when register, given invalid dependency, then throw exception`() {
        class B : TransientService
        class A(val b: B, val num: Int) : TransientService

        val container = ServiceContainer()

        val exception = assertThrows<InvalidDependencyException> {
            container.register(A::class, B::class)
        }
        assertThat(exception.message)
            .isEqualTo("Error registering 'A': Parameter 'num' must be either a Service or a ServiceProvider")
    }

    @Test
    fun `when register, given a ServiceProvider, then register service`() {
        class B : TransientService {
            val num = 4
        }

        class A(val provider: ServiceProvider<B>) : TransientService {
            fun getNum() =
                provider.get().num
        }

        val container = ServiceContainer()
        container.register(A::class, B::class)

        val a = container.get<A>()
        assertThat(a.getNum()).isEqualTo(4)
    }
}
