package bke.iso.service.v2

import org.junit.jupiter.api.Test

// TODO: finish tests
class ServiceContainerTest {

    @Test
    fun `when register, given nested dependency chain, then register and link all services`() {
        class C : TransientService
        class B(b: C) : TransientService
        class A(c: B) : TransientService

        val container = ServiceContainer()
        container.register(A::class)

        container.createInstance(A::class)
    }
}